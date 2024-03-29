package org.gbif.ocurrence.index.lucene;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.NoLockFactory;

import java.io.File;
import java.io.IOException;

/**
 * The initial version of an index is stored in a read-only FileSystem dir
 * (FileSystemDirectory). Index files created by newer versions are written to
 * a writable local FS dir (Lucene's FSDirectory). We should use the general
 * FileSystemDirectory for the writable dir as well. But have to use Lucene's
 * FSDirectory because currently Lucene does randome write and
 * FileSystemDirectory only supports sequential write.
 * Note: We may delete files from the read-only FileSystem dir because there
 * can be some segment files from an uncommitted checkpoint. For the same
 * reason, we may create files in the writable dir which already exist in the
 * read-only dir and logically they overwrite the ones in the read-only dir.
 */
public class MixedDirectory extends Directory {

  private final Directory readDir; // FileSystemDirectory
  private final Directory writeDir; // Lucene's FSDirectory

  // take advantage of the fact that Lucene's FSDirectory.fileExists is faster

  // for debugging
  MixedDirectory(Directory readDir, Directory writeDir) throws IOException {
    this.readDir = readDir;
    this.writeDir = writeDir;

    lockFactory = new NoLockFactory();
  }

  public MixedDirectory(FileSystem readFs, Path readPath, FileSystem writeFs, Path writePath, Configuration conf)
      throws IOException {

    try {
      readDir = new FileSystemDirectory(readFs, readPath, false, conf);
      // check writeFS is a local FS?
      writeDir = FSDirectory.open(new File(writePath.toString()));

    } catch (IOException e) {
      try {
        close();
      } catch (IOException e1) {
        // ignore this one, throw the original one
      }
      throw e;
    }

    lockFactory = new NoLockFactory();
  }

  @Override
  public void close() throws IOException {
    try {
      if (readDir != null) {
        readDir.close();
      }
    } finally {
      if (writeDir != null) {
        writeDir.close();
      }
    }
  }

  @Override
  public IndexOutput createOutput(String name) throws IOException {
    return writeDir.createOutput(name);
  }

  @Override
  public void deleteFile(String name) throws IOException {
    if (writeDir.fileExists(name)) {
      writeDir.deleteFile(name);
    }
    if (readDir.fileExists(name)) {
      readDir.deleteFile(name);
    }
  }

  @Override
  public boolean fileExists(String name) throws IOException {
    return writeDir.fileExists(name) || readDir.fileExists(name);
  }

  @Override
  public long fileLength(String name) throws IOException {
    if (writeDir.fileExists(name)) {
      return writeDir.fileLength(name);
    } else {
      return readDir.fileLength(name);
    }
  }

  @Override
  public long fileModified(String name) throws IOException {
    if (writeDir.fileExists(name)) {
      return writeDir.fileModified(name);
    } else {
      return readDir.fileModified(name);
    }
  }

  public String[] list() throws IOException {
    String[] readFiles = readDir.listAll();
    String[] writeFiles = writeDir.listAll();

    if (readFiles == null || readFiles.length == 0) {
      return writeFiles;
    } else if (writeFiles == null || writeFiles.length == 0) {
      return readFiles;
    } else {
      String[] result = new String[readFiles.length + writeFiles.length];
      System.arraycopy(readFiles, 0, result, 0, readFiles.length);
      System.arraycopy(writeFiles, 0, result, readFiles.length, writeFiles.length);
      return result;
    }
  }

  @Override
  public String[] listAll() throws IOException {
    return this.list();
  }

  @Override
  public IndexInput openInput(String name) throws IOException {
    if (writeDir.fileExists(name)) {
      return writeDir.openInput(name);
    } else {
      return readDir.openInput(name);
    }
  }

  @Override
  public IndexInput openInput(String name, int bufferSize) throws IOException {
    if (writeDir.fileExists(name)) {
      return writeDir.openInput(name, bufferSize);
    } else {
      return readDir.openInput(name, bufferSize);
    }
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "@" + readDir + "&" + writeDir;
  }

  @Override
  public void touchFile(String name) throws IOException {
    if (writeDir.fileExists(name)) {
      writeDir.touchFile(name);
    } else {
      readDir.touchFile(name);
    }
  }

}
