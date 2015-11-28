package automenta.rdp.rdp5.disk;

public interface DiskConst {
    
    static final String CLIENT_NAME = "CLOUDSOFT";
    
    static final String fs_label = "cloudsoft";
    static final String fs_type ="RDPFS";
    
    static final int RD_STATUS_SUCCESS = 0x00000000;
    static final int RD_STATUS_UNSUCCESS = 0x00000001;
    static final int RD_STATUS_NOT_IMPLEMENTED = 0x00000002;
    static final int RD_STATUS_PENDING = 0x00000103;
    static final int RD_STATUS_NOT_SUPPORTED = 0xc00000bb;
    static final int RD_STATUS_ACCESS_DENIED = 0xc0000022;
    static final int RD_STATUS_INVALID_HANDLE       =    0xc0000008;
    static final int RD_STATUS_INVALID_PARAMETER    =    0xc000000d;
    static final int RD_STATUS_NO_SUCH_FILE          =   0xc000000f;
    static final int RD_STATUS_INVALID_DEVICE_REQUEST=   0xc0000010;
    static final int RD_STATUS_OBJECT_NAME_COLLISION =   0xc0000035;
    static final int RD_STATUS_DISK_FULL             =   0xc000007f;
    static final int RD_STATUS_FILE_IS_A_DIRECTORY   =   0xc00000ba;
    static final int RD_STATUS_TIMEOUT               =   0xc0000102;
    static final int RD_STATUS_NOTIFY_ENUM_DIR       =   0xc000010c;
    static final int RD_STATUS_CANCELLED             =   0xc0000120;
    static final int RD_STATUS_DIRECTORY_NOT_EMPTY   =   0xc0000101;

    static final int RDPDR_CTYP_CORE = 0x4472;
    static final int RDPDR_CTYP_PRN = 0x5052;
    
    static final int PAKID_CORE_SERVER_ANNOUNCE = 0x496E;
    static final int PAKID_CORE_CLIENTID_CONFIRM = 0x4343;
    static final int PAKID_CORE_CLIENT_NAME = 0x434E;
    static final int PAKID_CORE_DEVICELIST_ANNOUNCE = 0x4441;
    static final int PAKID_CORE_DEVICE_REPLY = 0x6472;
    static final int PAKID_CORE_DEVICE_IOREQUEST = 0x4952;
    static final int PAKID_CORE_DEVICE_IOCOMPLETION = 0x4943;
    static final int PAKID_CORE_SERVER_CAPABILITY = 0x5350;
    static final int PAKID_CORE_CLIENT_CAPABILITY = 0x4350;
    static final int PAKID_CORE_DEVICELIST_REMOVE = 0x444D;
    static final int PAKID_CORE_USER_LOGGEDON = 0x554C;
    static final int PAKID_PRN_CACHE_DATA = 0x5043;
    static final int PAKID_PRN_USING_XPS = 0x5543;
    
    static final int CAP_GENERAL_TYPE = 0x0001;
    static final int CAP_PRINTER_TYPE = 0x0002;
    static final int CAP_PORT_TYPE = 0x0003;
    static final int CAP_DRIVE_TYPE = 0x0004;
    static final int CAP_SMARTCARD_TYPE = 0x0005;
    
    static final int GENERAL_CAPABILITY_VERSION_01 = 0x00000001;
    static final int GENERAL_CAPABILITY_VERSION_02 = 0x00000002;
    static final int PRINT_CAPABILITY_VERSION_01 = 0x00000001;
    static final int PORT_CAPABILITY_VERSION_01 = 0x00000001;
    static final int DRIVE_CAPABILITY_VERSION_01 = 0x00000001;
    static final int DRIVE_CAPABILITY_VERSION_02 = 0x00000002;
    static final int SMARTCARD_CAPABILITY_VERSION_01 = 0x00000001;
    
    static final int RDPDR_MINOR_RDP_VERSION_5_0 = 0x0002;
    static final int RDPDR_MINOR_RDP_VERSION_5_1 = 0x0005;
    static final int RDPDR_MINOR_RDP_VERSION_5_2 = 0x000A;
    static final int RDPDR_MINOR_RDP_VERSION_6_X = 0x000C;
    
    static final int RDPDR_DEVICE_REMOVE_PDUS = 0x00000001;
    static final int RDPDR_CLIENT_DISPLAY_NAME_PDU = 0x00000002;
    static final int RDPDR_USER_LOGGEDON_PDU = 0x00000004;
    
    static final int ENABLE_ASYNCIO = 0x00000001;
    
    static final int DEVICE_TYPE_DISK = 0x08;
    
    static final int IRP_MJ_CREATE = 0x00000000;
    static final int IRP_MJ_CLOSE = 0x00000002;
    static final int IRP_MJ_READ = 0x00000003;
    static final int IRP_MJ_WRITE = 0x00000004;
    static final int IRP_MJ_DEVICE_CONTROL = 0x0000000E;
    static final int IRP_MJ_QUERY_VOLUME_INFORMATION = 0x0000000A;
    static final int IRP_MJ_SET_VOLUME_INFORMATION = 0x0000000B;
    static final int IRP_MJ_QUERY_INFORMATION = 0x00000005;
    static final int IRP_MJ_SET_INFORMATION = 0x00000006;
    static final int IRP_MJ_DIRECTORY_CONTROL = 0x0000000C;
    static final int IRP_MJ_LOCK_CONTROL = 0x00000011;
    
    static final int FILE_SUPERSEDE     =         0x00000000;
    static final int FILE_OPEN          =    0x00000001;
    static final int FILE_CREATE        =     0x00000002;
    static final int FILE_OPEN_IF       =         0x00000003;
    static final int FILE_OVERWRITE     =         0x00000004;
    static final int FILE_OVERWRITE_IF  =         0x00000005;
    static final int FILE_MAXIMUM_DISPOSITION  =      0x00000005;
    
    static final int FILE_SUPERSEDED     =        0x00000000;
    static final int FILE_OPENED         =    0x00000001;
    static final int FILE_CREATED        =        0x00000002;
    static final int FILE_OVERWRITTEN    =        0x00000003;
    static final int FILE_EXISTS         =    0x00000004;
    static final int FILE_DOES_NOT_EXIST =        0x00000005;
    
    static final int FILE_DIRECTORY_FILE      =   0x00000001;
    static final int FILE_WRITE_THROUGH       =   0x00000002;
    static final int FILE_SEQUENTIAL_ONLY     =       0x00000004;
    static final int FILE_NO_INTERMEDIATE_BUFFERING   =   0x00000008;
    
    static final int FILE_DELETE_ON_CLOSE        =    0x00001000;
    static final int FILE_OPEN_BY_FILE_ID        =    0x00002000;
    static final int FILE_OPEN_FOR_BACKUP_INTENT =    0x00004000;
    static final int FILE_NO_COMPRESSION     =    0x00008000;
    
    static final int DELETE             =     0x00010000;
    static final int READ_CONTROL       =         0x00020000;
    static final int WRITE_DAC          =     0x00040000;
    static final int WRITE_OWNER        =     0x00080000;
    static final int SYNCHRONIZE        =     0x00100000;
    static final int STANDARD_RIGHTS_REQUIRED    =    0x000F0000;
    static final int STANDARD_RIGHTS_READ        =    0x00020000;
    static final int STANDARD_RIGHTS_WRITE       =    0x00020000;
    static final int STANDARD_RIGHTS_EXECUTE     =    0x00020000;
    static final int STANDARD_RIGHTS_ALL    =     0x001F0000;
    static final int SPECIFIC_RIGHTS_ALL    =     0x0000FFFF;
    static final int ACCESS_SYSTEM_SECURITY  =        0x01000000;
    static final int MAXIMUM_ALLOWED         =    0x02000000;
    
    static final int GENERIC_READ         =       0x80000000;
    static final int GENERIC_WRITE        =       0x40000000;
    static final int GENERIC_EXECUTE      =       0x20000000;
    static final int GENERIC_ALL          =   0x10000000;
    
    static final int FILE_READ_DATA = 0x0001;
    static final int FILE_LIST_DIRECTORY = 0x0001;
    static final int FILE_WRITE_DATA = 0x0002;
    static final int FILE_ADD_FILE = 0x0002;
    static final int FILE_APPEND_DATA = 0x0004;
    static final int FILE_ADD_SUBDIRECTORY = 0x0004;
    static final int FILE_CREATE_PIPE_INSTANCE = 0x0004;
    static final int FILE_READ_EA = 0x0008;
    static final int FILE_WRITE_EA = 0x0010;
    static final int FILE_EXECUTE = 0x0020;
    static final int FILE_TRAVERSE = 0x0020;
    static final int FILE_DELETE_CHILD = 0x0040;
    static final int FILE_READ_ATTRIBUTES = 0x0080;
    static final int FILE_WRITE_ATTRIBUTES = 0x0100;
    
    static final int FILE_ATTRIBUTE_READONLY     =    0x00000001;
    static final int FILE_ATTRIBUTE_HIDDEN       =    0x00000002;
    static final int FILE_ATTRIBUTE_SYSTEM       =    0x00000004;
    static final int FILE_ATTRIBUTE_NORMAL       =    0x00000080;
    static final int FILE_ATTRIBUTE_DIRECTORY    =    0x00000010;
    
    static final int FileBasicInformation = 0x00000004;
    static final int FileStandardInformation = 0x00000005;
    static final int FileAttributeTagInformation = 0x00000023;
    static final int FileEndOfFileInformation = 0x00000014;
    static final int FileDispositionInformation = 0x0000000D;
    static final int FileRenameInformation = 0x0000000A;
    static final int FileAllocationInformation = 0x00000013;
    
    static final int IRP_MN_QUERY_DIRECTORY         =  0x01;
    static final int IRP_MN_NOTIFY_CHANGE_DIRECTORY =  0x02;
    
    static final int RD_STATUS_NO_MORE_FILES        =    0x80000006;
    static final int RD_STATUS_DEVICE_PAPER_EMPTY   =    0x8000000e;
    static final int RD_STATUS_DEVICE_POWERED_OFF   =    0x8000000f;
    static final int RD_STATUS_DEVICE_OFF_LINE      =    0x80000010;
    static final int RD_STATUS_DEVICE_BUSY          =    0x80000011;
    
    static final int FileDirectoryInformation = 0x00000001;
    static final int FileFullDirectoryInformation = 0x00000002;
    static final int FileBothDirectoryInformation = 0x00000003;
    static final int FileNamesInformation = 0x0000000C;
    
    static final int FileFsVolumeInformation     = 1;
    static final int FileFsSizeInformation       = 3;
    static final int FileFsDeviceInformation     = 4;
    static final int FileFsFullSizeInformation   = 7;
    static final int FileFsAttributeInformation  = 5;
    
    static final int FILE_SUPPORTS_USN_JOURNAL = 0x02000000;
    static final int FILE_SUPPORTS_OPEN_BY_FILE_ID = 0x01000000;
    static final int FILE_SUPPORTS_EXTENDED_ATTRIBUTES = 0x00800000;
    static final int FILE_SUPPORTS_HARD_LINKS = 0x00400000;
    static final int FILE_SUPPORTS_TRANSACTIONS = 0x00200000;
    static final int FILE_SEQUENTIAL_WRITE_ONCE = 0x00100000;
    static final int FILE_READ_ONLY_VOLUME = 0x00080000;
    static final int FILE_NAMED_STREAMS = 0x00040000;
    static final int FILE_SUPPORTS_ENCRYPTION = 0x00020000;
    static final int FILE_SUPPORTS_OBJECT_IDS = 0x00010000;
    static final int FILE_VOLUME_IS_COMPRESSED = 0x00008000;
    static final int FILE_SUPPORTS_REMOTE_STORAGE = 0x00000100;
    static final int FILE_SUPPORTS_REPARSE_POINTS = 0x00000080;
    static final int FILE_SUPPORTS_SPARSE_FILES = 0x00000040;
    static final int FILE_VOLUME_QUOTAS = 0x00000020;
    static final int FILE_FILE_COMPRESSION = 0x00000010;
    static final int FILE_PERSISTENT_ACLS = 0x00000008;
    static final int FILE_UNICODE_ON_DISK = 0x00000004;
    static final int FILE_CASE_PRESERVED_NAMES = 0x00000002;
    static final int FILE_CASE_SENSITIVE_SEARCH = 0x00000001;
    
    static final int FILE_DEVICE_CD_ROM      =    0x00000002;
    static final int FILE_DEVICE_DISK        =    0x00000007;
    
}
