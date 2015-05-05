package nars.tuprolog.gui.ide;

import java.io.File;

public class FileIDE
{
    private String fileName;
    private String filePath;
    private String content;

    public FileIDE(String content, String filePath)
    {
        this.content = content;
        this.fileName = null;
        this.filePath = null;
        if (filePath!=null)
        {
            int i=filePath.lastIndexOf(File.separator, filePath.length());
            this.filePath = filePath.substring(0,i+1);
            this.fileName = filePath.substring(i+1);
        }
    }

    public String getFileName()
    {
        return fileName;
    }
    
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    public String getFilePath()
    {
        return filePath;
    }
    
    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
    
    public String getContent()
    {
        return content;
    }
    
    public void setContent(String content)
    {
        this.content = content;
    }

    public String getExtension()
    {
        String fileExtension = null;
        int i = getFileName().lastIndexOf('.');
        if (i > 0 && i < getFileName().length() - 1)
            fileExtension = getFileName().substring(i + 1).toLowerCase();
        return fileExtension;
    }
}
