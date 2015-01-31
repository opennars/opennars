/*
 * Easy text file operations
 * Copyright 2003, 2004 Edwin Martin <edwin@bitstorm.org>
 *
 */
package nars.ca.util;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


/**
 * Class for easy text file read and write operations.
 * @author Edwin Martin
 *
 */
public class EasyFile {
	private String filepath;
	private String filename;
	private InputStream textFileReader;
	private OutputStream textFileWriter;
	private final int bufferLength=1024;
	private Frame parent;
	private String title;
	private String fileExtension;

	/**
	 * Constructs a EasyFile. Open file by filename.
	 * @param filepath path of file
	 * @throws java.io.FileNotFoundException
	 */
	public EasyFile( String filepath ) {
		this.filepath = filepath;
		textFileReader = null;
		textFileWriter = null;
		fileExtension = null;
	}

	/**
	 * Constructs a EasyFile. Open file by url.
	 * @param url url of file
	 * @throws java.io.FileNotFoundException
	 */
	public EasyFile( URL url ) throws IOException {
		this.filepath = null;
		textFileWriter = null;
		fileExtension = null;
		
		textFileReader = url.openStream();
	}

	/**
	 * Constructs a EasyFile. Open file with file selector.
	 * @param parent parent frame
	 * @param title title of fileselector
	 * @throws java.io.FileNotFoundException
	 */
	public EasyFile( Frame parent, String title ) {
		this.parent = parent;
		this.title = title;
		textFileReader = null;
		textFileWriter = null;
		fileExtension = null;
	}
	
	/**
	 * Constructs a EasyFile. Read file from stream.
	 * @param textFileReader stream to read from
	 */
	public EasyFile( InputStream textFileReader ) {
		this.textFileReader = textFileReader;
		textFileWriter = null;
		filepath = null;
		fileExtension = null;
	}

	/**
	 * Constructs a EasyFile. Write file to stream.
	 * @param textFileWriter stream to write to
	 */
	public EasyFile( OutputStream textFileWriter ) {
		this.textFileWriter = textFileWriter;
		textFileReader = null;
		filepath = null;
		fileExtension = null;
	}

	/**
	 * Reads a text file into a string.
	 * @return contents of file
	 */
	public String readText() throws IOException {
		int bytesRead;

		if ( textFileReader == null ) {
			if ( filepath == null ) {
				FileDialog filedialog = new FileDialog( parent, title, FileDialog.LOAD );
				filedialog.setFile( filename );
				filedialog.show();
				if ( filedialog.getFile() != null ) {
					filename = filedialog.getFile();
					filepath = filedialog.getDirectory()+filename;
				} else
					return "";
			}
			textFileReader = new FileInputStream( filepath );
		}

		StringBuffer text = new StringBuffer();
		byte[] buffer = new byte[bufferLength];

		try {
			while ( ( bytesRead = textFileReader.read( buffer, 0, bufferLength ) ) != -1 )
				text.append( new String( buffer, 0, bytesRead ) );
		} finally {
			textFileReader.close();
		}
		return text.toString();
	}

	/**
	 * Writes a string to a text file.
	 * @param text text to write
	 */
	public void writeText( String text ) throws IOException {
		if ( textFileWriter == null ) {
			if ( filepath == null ) {
				FileDialog filedialog = new FileDialog( parent, title, FileDialog.SAVE );
				filedialog.setFile( filename );
				filedialog.show();
				if ( filedialog.getFile() != null ) {
					filename = filedialog.getFile();
					filepath = filedialog.getDirectory()+filename;
					if ( fileExtension != null && filepath.indexOf('.') == -1 ) {
						filepath = filepath+fileExtension;
					}
				} else
					return;
			}
			textFileWriter = new FileOutputStream( filepath );
		}

		try {
			textFileWriter.write( text.getBytes() );
		} finally {
			textFileWriter.close();
		}
	}
	
	/**
	 * Sets filename to use
	 * @param s filename
	 */
	public void setFileName( String s ) {
		filename = s;
	}

	/**
	 * Gets filename
	 * @return filename
	 */
	public String getFileName() {
		return filename == null ? filepath : filename;
	}

	/**
	 * Sets file extension to use
	 * @param s filename
	 */
	public void setFileExtension( String s ) {
		fileExtension = s;
	}
}
