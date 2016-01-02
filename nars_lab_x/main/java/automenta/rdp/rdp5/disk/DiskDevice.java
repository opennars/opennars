package automenta.rdp.rdp5.disk;

import automenta.rdp.AbstractRdpPacket;
import automenta.rdp.rdp.RdpPacket;
import automenta.rdp.rdp5.VChannel;
import automenta.rdp.tools.FNMatch;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DiskDevice implements Device, DiskConst {
    
    String diskName;
    String basePath;
    
    VChannel channel;
    
    private int id_sequence = 1;
    
    private BlockingQueue<IRP> irps;
    
    private boolean running = true;
    
    private Map<Integer, DriveFile> files;
    
    public DiskDevice(String diskName, String basePath) {
        super();
        this.diskName = diskName;
        this.basePath = basePath;
        
        files = new HashMap<Integer, DriveFile>();
        irps = new LinkedBlockingQueue<IRP>();
        Thread t = new Thread(new ProcessThread(), "DiskDevice_Thread_" + this.hashCode());
        t.start();
    }

    @Override
    public int getType() {
        return DEVICE_TYPE_DISK;
    }

    @Override
    public String getName() {
        return diskName;
    }
    
    @Override
    public void setChannel(VChannel channel) {
        this.channel = channel;
    }

    @Override
    public int process(AbstractRdpPacket data, IRP irp) throws IOException {
//        System.out.print("执行" + irp.majorFunction + ", fileId=" + irp.fileId);
        DriveFile df = files.get(irp.fileId);
//        if(df != null) {
//            System.out.println(",filePath=" + df.fullPath);
//        } else {
//            System.out.println();
//        }
        
        switch(irp.majorFunction) {
        case IRP_MJ_READ:
        case IRP_MJ_WRITE:
            if(irps.offer(irp)) {
                return RD_STATUS_PENDING;
            } else {
                return RD_STATUS_CANCELLED;
            }
        default:
            return process0(data, irp);
        }
    }
    
    private int process0(AbstractRdpPacket data, IRP irp) throws IOException {
        int status;
        switch(irp.majorFunction) {
        case IRP_MJ_CREATE:
            status = drive_process_irp_create(data, irp);
            break;

        case IRP_MJ_CLOSE:
            status = drive_process_irp_close(data, irp);
            break;

        case IRP_MJ_READ:
            status = drive_process_irp_read(data, irp);
            break;

        case IRP_MJ_WRITE:
            status = drive_process_irp_write(data, irp);
            break;

        case IRP_MJ_QUERY_INFORMATION:
            status = drive_process_irp_query_information(data, irp);
            break;

        case IRP_MJ_SET_INFORMATION:
            status = drive_process_irp_set_information(data, irp);
            break;

        case IRP_MJ_QUERY_VOLUME_INFORMATION:
            status = drive_process_irp_query_volume_information(data, irp);
            break;

        case IRP_MJ_LOCK_CONTROL:
            status = drive_process_irp_silent_ignore(data, irp);
            break;

        case IRP_MJ_DIRECTORY_CONTROL:
            status = drive_process_irp_directory_control(data, irp);
            break;

        case IRP_MJ_DEVICE_CONTROL:
            status = drive_process_irp_device_control(data, irp);
            break;

        default:
            status = RD_STATUS_NOT_SUPPORTED;
            break;
        }
        
        return status;
    }
    
    private int drive_process_irp_create(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        int desiredAccess = data.getLittleEndian32();
        data.positionAdd(16);/* AllocationSize(8), FileAttributes(4), SharedAccess(4) */
        int createDisposition = data.getLittleEndian32();
        int createOptions = data.getLittleEndian32();
        int pathLength = data.getLittleEndian32();
        
        int ioStatus, fileId, information;
        
        String fileName = ""; 
        if(pathLength > 0 && (pathLength / 2) < 256) {
            byte[] pathByte = new byte[pathLength];
            data.copyToByteArray(pathByte, 0, data.position(), pathLength);
            fileName = parsePath(pathByte);
            fileName = fileName.replaceAll("\\\\", "/");
            if(fileName.endsWith("/")) {
                fileName = fileName.substring(0, fileName.length() - 1);
            }
        }
        fileId = id_sequence++;
        DriveFile df = drive_file_new(basePath, fileName, fileId, desiredAccess, createDisposition, createOptions);
        if(df == null) {
            fileId = 0;
            information = 0;
            ioStatus = RD_STATUS_UNSUCCESS;
        } else if(df.err != 0) {
            fileId = 0;
            information = 0;
            ioStatus = df.err;
        } else {
            files.put(fileId, df);
            switch (createDisposition) {
                case FILE_SUPERSEDE:
                case FILE_OPEN:
                case FILE_CREATE:
                case FILE_OVERWRITE:
                    information = FILE_SUPERSEDED;
                    break;
                case FILE_OPEN_IF:
                    information = FILE_OPENED;
                    break;
                case FILE_OVERWRITE_IF:
                    information = FILE_OVERWRITTEN;
                    break;
                default:
                    information = 0;
                    break;
            }
            ioStatus = RD_STATUS_SUCCESS;
        }
        writeIntLe(out, fileId);
        out.write(information);
        return ioStatus;
    }
    
    private int drive_process_irp_close(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        DriveFile df = files.get(irp.fileId);
        if(df == null || !df.file.exists()) {
            return RD_STATUS_UNSUCCESS;
        }
        
        df.closeRaf();
        
        if(df.delete_pending) {
            df.file.delete();
        }
        files.remove(irp.fileId);
        
        //5 bytes padding
        out.writeInt(0);
        out.write(0);
        
        return RD_STATUS_SUCCESS;
    }
    
    private int drive_process_irp_read(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        DriveFile df = files.get(irp.fileId);
        if (df == null || !df.file.exists()) {
            out.writeInt(0);
            return RD_STATUS_UNSUCCESS;
        }
        int length = data.getLittleEndian32();
        long offset = ((long)data.getLittleEndian32()) + (((long)data.getLittleEndian32()) << 32);
        
        long fileLength = df.file.length();
        if((offset + length) > fileLength) {
            length = (int) (fileLength - offset);
        }
        
        try {
            RandomAccessFile raf = df.getRAF(false);
            raf.seek(offset);
            byte[] bf = new byte[length];
            raf.readFully(bf);
            writeIntLe(out, length);
            out.write(bf);
        } catch (Exception e) {
            out.writeInt(0);
            return RD_STATUS_UNSUCCESS;
        }
        
        return RD_STATUS_SUCCESS;
    }
    
    private int drive_process_irp_write(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        int length = data.getLittleEndian32();
        long offset = ((long)data.getLittleEndian32()) + (((long)data.getLittleEndian32()) << 32);
        data.positionAdd(20);
        
        DriveFile df = files.get(irp.fileId);
        if(df == null) {
            out.writeInt(0);
            out.write(0);
            return RD_STATUS_INVALID_HANDLE;
        }
        try {
            RandomAccessFile raf = df.getRAF(true);
            raf.seek(offset);
            byte[] bf = new byte[length];
            data.copyToByteArray(bf, 0, data.position(), length);
            raf.write(bf);
//            for(int l = 0; l < length; l++) {
//                raf.write(data.get8());
//            }
            writeIntLe(out, length);
            out.write(0);
        } catch (FileNotFoundException e) {
            out.writeInt(0);
            out.write(0);
            return RD_STATUS_UNSUCCESS;
        }
        
        
        return RD_STATUS_SUCCESS;
    }
    
    private int drive_process_irp_query_information(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        int fsInformationClass = data.getLittleEndian32();
        
        DriveFile df = files.get(irp.fileId);
        if(df == null || !df.file.exists()) {
            out.writeInt(1);//length
            out.write(0);
            return RD_STATUS_ACCESS_DENIED;
        }
        File f = df.file; //new File(df.file);
        int file_attributes = 0;
        if (df.file.isDirectory()) {
            file_attributes |= FILE_ATTRIBUTE_DIRECTORY;
        }
        if (df.file.isHidden()) {
            file_attributes |= FILE_ATTRIBUTE_HIDDEN;
        }
        if (file_attributes == 0) {
            file_attributes |= FILE_ATTRIBUTE_NORMAL;
        }
        if (!f.canWrite()) {
            file_attributes |= FILE_ATTRIBUTE_READONLY;
        }
        switch (fsInformationClass) {
        case FileBasicInformation:

            long now = System.currentTimeMillis();
            long createTime = now; //TODO getWindowsTime(subf.getCreationTime());
            long lastAccessTime = now; //TODO getWindowsTime(subf.getLastAccessTime());
            long lastWriteTime = now; //TODO getWindowsTime(subf.getLastModifiedTime());


            /* http://msdn.microsoft.com/en-us/library/cc232094.aspx */
//            long createTime = getWindowsTime(createTime);
//            long lastAccessTime = getWindowsTime(f.getLastAccessTime());
//            long lastWriteTime = getWindowsTime(f.getLastModifiedTime());
            
            writeIntLe(out, 36);//length
            writeLongLe(out, createTime);
            writeLongLe(out, lastAccessTime);
            writeLongLe(out, lastWriteTime);
            writeLongLe(out, lastWriteTime);
            
            writeIntLe(out, file_attributes);
            break;

        case FileStandardInformation:
            /*  http://msdn.microsoft.com/en-us/library/cc232088.aspx */
            writeIntLe(out, 22); /* Length */
            writeLongLe(out, df.file.length());/* AllocationSize */
            writeLongLe(out, df.file.length()); /* EndOfFile */
            writeIntLe(out, 0); /* NumberOfLinks */
            out.write(df.delete_pending ? 1 : 0); /* DeletePending */
            out.write(df.isDir ? 1 : 0); /* Directory */
            /* Reserved(2), MUST NOT be added! */
            break;

        case FileAttributeTagInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232093.aspx */
            writeIntLe(out, 8); /* Length */
            writeIntLe(out, file_attributes); /* FileAttributes */
            writeIntLe(out, 0); /* ReparseTag */
            break;
        default:
            out.writeInt(1);//length
            out.write(0);
            return RD_STATUS_UNSUCCESS;
        }
        
        return RD_STATUS_SUCCESS;
    }
    
    private int drive_process_irp_set_information(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        int fsInformationClass = data.getLittleEndian32();
        int length = data.getLittleEndian32();
        data.positionAdd(24);
        DriveFile df = files.get(irp.fileId);
        if(df == null || !df.file.exists()) {
            out.writeInt(length);//length
            out.write(0);//padding
            return RD_STATUS_ACCESS_DENIED;
        }
        switch(fsInformationClass) {
        case FileBasicInformation:
            //do nothing

            File f = df.file;
//            of.file.set
//            f.set
            long createTime = data.getLittleEndian32() + (((long)data.getLittleEndian32()) << 32);
            long accessTime = parseWindowsTime((long)data.getLittleEndian32() + ((long)(data.getLittleEndian32()) << 32));
            long writeTime = parseWindowsTime((long)data.getLittleEndian32() + ((long)(data.getLittleEndian32()) << 32));
            long changeTime = parseWindowsTime((long)data.getLittleEndian32() + ((long)(data.getLittleEndian32()) << 32));
            int fileAttributes = data.getLittleEndian32();
            
//            df.file.setLastModified(writeTime);
            out.writeInt(length);//length
            out.write(0);//padding
            return RD_STATUS_UNSUCCESS;
            
//            break;
        case FileEndOfFileInformation:
            // we can do nothing
            break;
        case FileDispositionInformation://This information class is used to mark a file for deletion
            if(length > 0) {
                df.delete_pending = (data.get8() != 0);
            } else {
                df.delete_pending = true;
            }
            if(df.delete_pending) {
                if(df.file.isDirectory()) {
                    String[] fs = df.file.list();
                    if(fs != null && fs.length > 0) {
                        out.writeInt(0);//length
                        out.write(0);//padding
                        return RD_STATUS_DIRECTORY_NOT_EMPTY;
                    }
                }
            }
            break;
        case FileRenameInformation:
            int replaceIfExists = data.get8();
            int rootDirectory = data.get8();//RootDirectory
            int pathLength = data.getLittleEndian32();
            String fileName = ""; 
            if(pathLength > 0 && (pathLength / 2) < 256) {
                byte[] pathByte = new byte[pathLength];
                data.copyToByteArray(pathByte, 0, data.position(), pathLength);
                fileName = parsePath(pathByte);
                fileName = fileName.replaceAll("\\\\", "/");
            } else {
                out.writeInt(length);//length
                out.write(0);//padding
                return RD_STATUS_INVALID_PARAMETER;
            }
            if(!df.file.renameTo(new File(basePath, fileName))) {
                out.writeInt(length);//length
                out.write(0);//padding
                return RD_STATUS_ACCESS_DENIED;
            }
            break;
        case FileAllocationInformation:
            break;
        default :
            out.writeInt(length);//length
            out.write(0);//padding
            return RD_STATUS_INVALID_PARAMETER;
        }
        
        out.writeInt(length);//length
        out.write(0);//padding
        return RD_STATUS_SUCCESS;
    }
    
    private int drive_process_irp_query_volume_information(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        int fsInformationClass = data.getLittleEndian32();
//        int length = data.getLittleEndian32();
//        data.incrementPosition(24);
        
        int serial = 1;
        
        int length = 0;
        
        switch(fsInformationClass) {
        case FileFsVolumeInformation:
            length = 2 * fs_label.length();
            writeIntLe(out, 17 + length);
            out.writeLong(0);//VolumeCreationTime
            writeIntLe(out, serial);
            writeIntLe(out, length);
            
            out.write(0);/* SupportsObjects */
            /* Reserved(1), MUST NOT be added! */
            for(int i = 0; i < fs_label.length(); i++) {
                char c = fs_label.charAt(i);
                out.write((byte) c);
                out.write((byte) (c >> 8));
            }
            break;

        case FileFsSizeInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232107.aspx */
            writeIntLe(out, 24); /* Length */
            writeLongLe(out, 10L * 1024 * 1024);
            writeLongLe(out, 5L * 1024 * 1024);//可用
            writeIntLe(out, 4 * 1024 / 0x200);//8 sectors/unit
            writeIntLe(out, 0x200);//512 bytes/sector
            break;

        case FileFsAttributeInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232101.aspx */
            length = 2 * fs_type.length();
            writeIntLe(out, 12 + length); /* Length */
            writeIntLe(out, FILE_CASE_PRESERVED_NAMES | FILE_CASE_SENSITIVE_SEARCH | FILE_UNICODE_ON_DISK); /* FileSystemAttributes */
            writeIntLe(out, 0xFF);/* MaximumComponentNameLength */
            writeIntLe(out, length);
            for(int i = 0; i < fs_type.length(); i++) {
                char c = fs_type.charAt(i);
                out.write((byte) c);
                out.write((byte) (c >> 8));
            }
            break;

        case FileFsFullSizeInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232104.aspx */
            writeIntLe(out, 32); /* Length */
            writeLongLe(out, 10L * 1024 * 1024);
            writeLongLe(out, 5L * 1024 * 1024);//可用
            writeLongLe(out, 6L * 1024 * 1024);//free
            writeIntLe(out, 4 * 1024 / 0x200);
            writeIntLe(out, 0x200);
            break;

        case FileFsDeviceInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232109.aspx */
            writeIntLe(out, 8); /* Length */
            writeIntLe(out, FILE_DEVICE_DISK);/* DeviceType */
            writeIntLe(out, 0);/* Characteristics */
            break;

        default:
            out.writeInt(0);
            return RD_STATUS_UNSUCCESS;
        }
        
        return RD_STATUS_SUCCESS;
    }
    
    private static int drive_process_irp_silent_ignore(AbstractRdpPacket data, IRP irp) throws IOException {
        irp.out.writeInt(0);//length
        return RD_STATUS_SUCCESS;
    }
    
    private int drive_process_irp_directory_control(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        switch (irp.minorFunction) {
        case IRP_MN_QUERY_DIRECTORY:
            return drive_process_irp_query_directory(data, irp);
        case IRP_MN_NOTIFY_CHANGE_DIRECTORY:
            return disk_create_notify(data, irp);//length;
        default:
            out.writeInt(0);//length;
            return RD_STATUS_NOT_SUPPORTED;
        }
    }
    
    private int disk_create_notify(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        int fsInformationClass = data.getLittleEndian32();
        
        out.writeInt(0);//length

        int result = RD_STATUS_PENDING;
        
        DriveFile df = files.get(irp.fileId);
        if(!df.file.exists() || !df.file.isDirectory()) {
            result = RD_STATUS_ACCESS_DENIED;
        }
        
        if ((fsInformationClass & 0x1000) != 0) { 
            if (result == RD_STATUS_PENDING) {
                return RD_STATUS_SUCCESS;
            }
        }
        return result;
    }
    
    private int drive_process_irp_query_directory(AbstractRdpPacket data, IRP irp) throws IOException {
        DataOutputStream out = irp.out;
        int fsInformationClass = data.getLittleEndian32();
        int initialQuery = data.get8();
        int pathLength = data.getLittleEndian32();
        data.positionAdd(23);
        
        int file_attributes = 0;
        
        String subFile = null;
        File subJavaFile = null;
        //javaxt.io.File subf = null;
        
        String pattern = "";
        if (pathLength > 0 && pathLength < 2 * 255) {
            byte[] pathByte = new byte[pathLength];
            data.copyToByteArray(pathByte, 0, data.position(), pathLength);
            pattern = parsePath(pathByte);
            pattern = pattern.replaceAll("\\\\", "/");
        }
        DriveFile df = files.get(irp.fileId);
        if(df == null) {
            writeIntLe(out, 1);
            out.write(0);//padding
            return RD_STATUS_ACCESS_DENIED;
        }
        
        if(!df.isDir) {
            writeIntLe(out, 1);
            out.write(0);//padding
            return RD_STATUS_NO_MORE_FILES;
        }
        
        if(initialQuery != 0) {
            if(pattern.length() != 0) {
                int index = pattern.lastIndexOf('/');
                if(index != -1) {
                    df.pattern = pattern.substring(index);
                } else {
                    df.pattern = pattern;
                }
                String[] files = null;
                files = df.file.list();
                df.subfiles = Arrays.asList(files == null ? new String[]{} : files).iterator();
            }
        }
        
        while(df.subfiles.hasNext()) {
            subFile = df.subfiles.next();
            if(FNMatch.fnmatch(df.pattern, '/' + subFile, 0)) {
                break;
            }
            subFile = null;
        }
        
        if(subFile == null) {
            writeIntLe(out, 1);
            out.write(0);//padding
            return RD_STATUS_NO_MORE_FILES;// STATUS_NO_MORE_FILES;
        }
        
        subJavaFile = new File(df.file, subFile);
        File subf = subJavaFile;
        //subf = new javaxt.io.File(subJavaFile);
        
        if (subJavaFile.isDirectory()) {
            file_attributes |= FILE_ATTRIBUTE_DIRECTORY;
        }
        if (!subf.canRead()) {
            file_attributes |= FILE_ATTRIBUTE_HIDDEN;
        }
        if (file_attributes == 0) {
            file_attributes |= FILE_ATTRIBUTE_NORMAL;
        }
        if (!subf.canWrite()) {
            file_attributes |= FILE_ATTRIBUTE_READONLY;
        }

        long now = System.currentTimeMillis();
        long createTime = now; //TODO getWindowsTime(subf.getCreationTime());
        long lastAccessTime = now; //TODO getWindowsTime(subf.getLastAccessTime());
        long lastWriteTime = now; //TODO getWindowsTime(subf.getLastModifiedTime());
        
        int length = 2 * subFile.length() + 2;
        
        switch(fsInformationClass) {
        case FileDirectoryInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232097.aspx */
            writeIntLe(out, 64 + length); /* Length */
            writeIntLe(out, 0); /* NextEntryOffset */
            writeIntLe(out, 0); /* FileIndex */
            writeLongLe(out, createTime);
            writeLongLe(out, lastAccessTime);
            writeLongLe(out, lastWriteTime);
            writeLongLe(out, lastWriteTime);

            long size = Files.size(Paths.get(subf.getAbsolutePath()));
            writeLongLe(out, size);/* Allocation size */
            writeLongLe(out, size);/* End of file */
            
            writeIntLe(out, file_attributes); /* FileAttributes */
            
            writeIntLe(out, length);
            writePath(out, subFile);
            
            break;

        case FileFullDirectoryInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232068.aspx */
            writeIntLe(out, 68 + length); /* Length */
            writeIntLe(out, 0); /* NextEntryOffset */
            writeIntLe(out, 0); /* FileIndex */
            writeLongLe(out, createTime);
            writeLongLe(out, lastAccessTime);
            writeLongLe(out, lastWriteTime);
            writeLongLe(out, lastWriteTime);

            long s2 = Files.size(Paths.get(subf.getAbsolutePath()));
            writeLongLe(out, s2);/* Allocation size */
            writeLongLe(out, s2);/* End of file */

            writeIntLe(out, file_attributes); /* FileAttributes */
            
            writeIntLe(out, length);
            writeIntLe(out, 0);//EaSize
            writePath(out, subFile);
            break;

        case FileBothDirectoryInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232095.aspx */
            writeIntLe(out, 93 + length); /* Length */
            writeIntLe(out, 0); /* NextEntryOffset */
            writeIntLe(out, 0); /* FileIndex */
            writeLongLe(out, createTime);
            writeLongLe(out, lastAccessTime);
            writeLongLe(out, lastWriteTime);
            writeLongLe(out, lastWriteTime);

            long s3 = Files.size(Paths.get(subf.getAbsolutePath()));
            writeLongLe(out, s3);/* Allocation size */
            writeLongLe(out, s3);/* End of file */

            writeIntLe(out, file_attributes); /* FileAttributes */
            
            writeIntLe(out, length);
            writeIntLe(out, 0);//EaSize
            out.write(0); /* ShortNameLength */
            /* Reserved(1), MUST NOT be added! */
            ////////////////012345678901
            out.writeChars("            "); /* ShortName, 24 bytes */
            writePath(out, subFile);
            break;

        case FileNamesInformation:
            /* http://msdn.microsoft.com/en-us/library/cc232077.aspx */
            writeIntLe(out, 12 + length); /* Length */
            writeIntLe(out, 0); /* NextEntryOffset */
            writeIntLe(out, 0); /* FileIndex */
            writeIntLe(out, length); /* FileNameLength */
            writePath(out, subFile);
            break;

        default:
            writeIntLe(out, 1);
            out.write(0); /* Padding */
            return RD_STATUS_NO_MORE_FILES;
        }
        
        return RD_STATUS_SUCCESS;
    }
    
    private static int drive_process_irp_device_control(AbstractRdpPacket data, IRP irp) throws IOException {
        irp.out.writeInt(0);
        return RD_STATUS_NOT_SUPPORTED;
    }
    
    private static String parsePath(byte[] unicodeBytes) {
        StringBuilder sb = new StringBuilder("");
        int i = 0;
        while(i < unicodeBytes.length) {
            char c = (char) ((0xFF&unicodeBytes[i]) | ((0xFF&unicodeBytes[i+1]) << 8));
            i += 2;
            if(c != 0) {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
    
    private static void writeLongLe(DataOutputStream out, long v) throws IOException {
        out.write((byte)(v >>>  0));
        out.write((byte)(v >>>  8));
        out.write((byte)(v >>> 16));
        out.write((byte)(v >>> 24));
        out.write((byte)(v >>> 32));
        out.write((byte)(v >>> 40));
        out.write((byte)(v >>> 48));
        out.write((byte)(v >>> 56));
    }
    
    private static void writeIntLe(DataOutputStream out, int v) throws IOException {
        out.write((byte)(v >>>  0));
        out.write((byte)(v >>>  8));
        out.write((byte)(v >>> 16));
        out.write((byte)(v >>> 24));
    }
    
    private static long getWindowsTime(Date date) {
        if(date == null) {
            date = new Date();
        }
        return (date.getTime() + 11644473600000L) * 10000;
    }
    
    private static long parseWindowsTime(long t) {
        return t / 10000 - 11644473600000L;
    }
    
    private static void writePath(DataOutputStream out, String path) throws IOException {
        for(int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            out.write((byte) c);
            out.write((byte) (c >> 8));
        }
        out.write(0);//终结符
        out.write(0);
    }
    
    private DriveFile drive_file_new(String basePath, String fileName, int id,
            int desiredAccess, int createDisposition, int createOptions) throws IOException {
        DriveFile df = new DriveFile();
        df.id = id;
        df.basePath = basePath;
        df.fullPath = basePath + fileName;
        File f = new File(df.fullPath);
        df.file = f;
        if (!drive_file_init(df, desiredAccess, createDisposition, createOptions)) {
            return null;
        }
        return df;
    }
    
    private class ProcessThread implements Runnable {
        @Override
        public void run() {
            try {
                while(running) {
                    IRP irp = irps.take();
                    if(irp != null) {
                        callProcess0(irp);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void callProcess0(IRP irp) {
        int ioStatus = 0;
        byte[] buffer = null;
        try {
            ioStatus = process0(irp.data, irp);
            irp.out.flush();
            irp.bout.flush();
            buffer = irp.bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(ioStatus != RD_STATUS_PENDING) {
            //device i/o response header
            RdpPacket s = new RdpPacket(16 + buffer.length);
            s.setLittleEndian16(RDPDR_CTYP_CORE);// PAKID_CORE_DEVICE_REPLY?
            s.setLittleEndian16(PAKID_CORE_DEVICE_IOCOMPLETION);
            s.setLittleEndian32(irp.deviceId);
            s.setLittleEndian32(irp.completionId);
            s.setLittleEndian32(ioStatus);
            
            if(buffer.length > 0) {
                s.copyFromByteArray(buffer, 0, s.position(), buffer.length);
            }
            
            s.markEnd();
            try {
                this.channel.send_packet(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean drive_file_init(DriveFile df, int desiredAccess, int createDisposition, int createOptions) throws IOException {
        if(df.file.exists()) {
            df.isDir = df.file.isDirectory();
        } else {
            df.isDir = (createOptions & FILE_DIRECTORY_FILE) != 0;
            if(df.isDir) {
                if ((createDisposition == FILE_OPEN_IF) || (createDisposition == FILE_CREATE)) {
                    try {
                        if(!df.file.mkdir()) {
                            df.err = RD_STATUS_ACCESS_DENIED;
                            return true;
                        }
                    } catch (Exception e) {
                        df.err = RD_STATUS_ACCESS_DENIED;
                        return true;
                    }
                }
            }
        }
        if(!df.isDir) {
            switch (createDisposition) {
            case FILE_SUPERSEDE:
                if(df.file.exists()) {
                    df.file.delete();
                }
                df.file.createNewFile();
                break;
            case FILE_OPEN:
                if(!df.file.exists()) {
                    df.err = RD_STATUS_NO_SUCH_FILE;
                    return true;
                }
                break;
            case FILE_CREATE:
                if(df.file.exists()) {
                    df.err = RD_STATUS_ACCESS_DENIED;
                    return true;
                } else {
                    df.file.createNewFile();
                }
                break;
            case FILE_OPEN_IF:
                if(!df.file.exists()) {
                    df.file.createNewFile();
                }
                break;
            case FILE_OVERWRITE:
                if(df.file.exists()) {
                    df.file.delete();
                    df.file.createNewFile();
                } else {
                    df.err = RD_STATUS_NO_SUCH_FILE;
                    return true;
                }
                break;
            case FILE_OVERWRITE_IF:
                if(df.file.exists()) {
                    df.file.delete();
                }
                df.file.createNewFile();
                break;
            default:
                break;
            }
            if ((createOptions & FILE_DELETE_ON_CLOSE) != 0 && (desiredAccess & DELETE) != 0) {
                df.delete_pending = true;
            }
            df.desiredAccess = desiredAccess;
//            if ((desiredAccess & GENERIC_ALL) != 0
//                    || (desiredAccess & GENERIC_WRITE) != 0
//                    || (desiredAccess & FILE_WRITE_DATA) != 0
//                    || (desiredAccess & FILE_APPEND_DATA) != 0) {
//                df.file.delete();
//                df.file.createNewFile();
//            }
        }
        
        return true;
    }
    
    private class DriveFile {
        Iterator<String> subfiles;
        int id;
        boolean isDir;
        int err;
        File file;
        String basePath;
        String fullPath;
        String fileName;
        String pattern;
        int desiredAccess;
        boolean delete_pending;
        RandomAccessFile raf = null;
        synchronized RandomAccessFile getRAF(boolean write) throws IOException {
            if(raf == null) {
                raf = new RandomAccessFile(file, "rw");
                if (write && ((desiredAccess & GENERIC_ALL) != 0
                        || (desiredAccess & GENERIC_WRITE) != 0
                        || (desiredAccess & FILE_WRITE_DATA) != 0
                        || (desiredAccess & FILE_APPEND_DATA) != 0)) {
                    raf.setLength(0);//clean
                }
            }
            return raf;
        }
        synchronized void closeRaf() {
            if(raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                }
            }
            raf = null;
        }
    }
    
}
