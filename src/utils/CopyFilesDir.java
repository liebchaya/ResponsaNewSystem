package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class CopyFilesDir {

 
  public static final void copy( File source, File destination ) throws IOException {
	  System.out.println("Start copying...");
    if( source.isDirectory() ) {
      copyDirectory( source, destination );
    } else {
      copyFile( source, destination );
    }
  }
 
  public static final void copyDirectory( File source, File destination ) throws IOException {
    if( !source.isDirectory() ) {
      throw new IllegalArgumentException( "Source (" + source.getPath() + ") must be a directory." );
    }
   
    if( !source.exists() ) {
      throw new IllegalArgumentException( "Source directory (" + source.getPath() + ") doesn't exist." );
    }
   
    if( destination.exists() ) {
      throw new IllegalArgumentException( "Destination (" + destination.getPath() + ") exists." );
    }
   
    destination.mkdirs();
    File[] files = source.listFiles();
   
    for( File file : files ) {
      if( file.isDirectory() ) {
        copyDirectory( file, new File( destination, file.getName() ) );
    	// converting directory and file names to sequential numbers
//        copyDirectory( file, new File( destination, Integer.toString(m_dirId++) ) );
      } else {
    	  copyFile( file, new File( destination, file.getName()) );
//    	String fileExten = file.getName().substring(file.getName().indexOf("."));
//        copyFile( file, new File( destination, Integer.toString(m_fileId++)+fileExten ) );
    	  
      }
    }
  }
 
  public static final void copyFile( File source, File destination ) throws IOException {
    FileChannel sourceChannel = new FileInputStream( source ).getChannel();
    FileChannel targetChannel = new FileOutputStream( destination ).getChannel();
    sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
    sourceChannel.close();
    targetChannel.close();
  }
  
  private static int m_fileId = 0;
  private static int m_dirId = 0;
  
  public static void main(String[] args) throws Exception {
	  copy(new File("./Techumin/tchum29"), new File("./Techumin/tchum29Copy"));
  }
}
