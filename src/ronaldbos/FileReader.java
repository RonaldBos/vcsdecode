package ronaldbos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReader {
    private static final int READ_AHEAD_LIMIT = 64 * 1024;
    private BufferedReader reader;

    public FileReader() {
        this.reader = null;
    }
    
    private BufferedReader getInputStream(String fileName) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
    }
    
    public void open(String fileName) throws FileNotFoundException {
        if (this.reader != null) {
            throw new IllegalStateException("already opened");
        }
        this.reader = getInputStream(fileName);
    }
    
    public String readLine() throws IOException {
        if (reader == null) {
            throw new IllegalStateException("not open");
        }
        reader.mark(READ_AHEAD_LIMIT);
        return reader.readLine();
    }
    
    public void reset() throws IOException {
        if (reader == null) {
            throw new IllegalStateException("not open");
        }
        reader.reset();
    }
    
    public String peekLine() throws IOException {
        if (reader == null) {
            throw new IllegalStateException("not open");
        }
        reader.mark(READ_AHEAD_LIMIT);
        String result = reader.readLine();
        reader.reset();
        return result;
    }
    
    public void close() throws IOException {
        if (this.reader == null) {
            throw new IllegalStateException("not open");
        }
        this.reader.close();
    }
}
