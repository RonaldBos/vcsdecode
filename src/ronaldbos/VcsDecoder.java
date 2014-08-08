package ronaldbos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

public class VcsDecoder {
    private static final String TAG_ATTENDEE = "ATTENDEE";
    private static final String BEGIN_VEVENT = "BEGIN:VEVENT";
    private static final String END_VEVENT = "END:VEVENT";
    private static final String QUOTED_PRINTABLE = "QUOTED-PRINTABLE";
    private static final String ATTENDEE_ORGANIZER = "X-RELATIONSHIP=ORGANIZER";
    
    private static final List<String> KEEP_ORGANIZERS = new ArrayList<String>() {{
        add("Ronald Bos");
        add("Ned Laver");
    }};
    
    private FileReader reader;
    private BufferedWriter writer;
    
    public VcsDecoder() {
        this.reader = null;
        this.writer = null;
    }
    
    public void decode(String inFileName, String outFileName) throws IOException, DecoderException {
        if (reader != null || writer != null) {
            throw new IllegalStateException("already decoding");
        }
        
        reader = new FileReader();
        reader.open(inFileName);
        writer = getOutputStream(outFileName);
        
        handleHeader();
        handleEvents();
        handleFooter();
        
        reader.close();
        writer.flush();
        writer.close();
    }
    
    private BufferedWriter getOutputStream(String fileName) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName))));
    }
    
    private void handleHeader() throws IOException, DecoderException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals(BEGIN_VEVENT)) {
                reader.reset();
                break;
            }
            writer.write(line);
            writer.newLine();
        }
    }
    
    class Event {
        String organizer;
        String decodedEvent;
        
        Event(String organizer, String decoded) {
            this.organizer = organizer;
            this.decodedEvent = decoded;
        }
        
        @Override
        public String toString() {
            return this.decodedEvent;
        }
    }
    
    private void handleEvents() throws IOException, DecoderException {
        for (Event event = readEvent(); event != null; event = readEvent()) {
            if (!KEEP_ORGANIZERS.contains(event.organizer)) {
                System.out.println("Discarding event organized by " + event.organizer);
                continue;
            }
            writer.write(event.toString());
            writer.newLine();
        }
    }
    
    private Event readEvent() throws IOException, DecoderException {
        String line = reader.readLine();
        String result = line + "\n";
        String organizer = null;
        if (!line.equals(BEGIN_VEVENT)) {
            reader.reset();
            return null;
        }
        while ((line = reader.readLine()) != null) {
            if (line.equals(END_VEVENT)) {
                result += line;
                break;
            } else if (line.contains(QUOTED_PRINTABLE)) {
                result += decodeLine(line);
            } else if (line.startsWith(TAG_ATTENDEE) && line.contains(ATTENDEE_ORGANIZER)) {
                organizer = parseCn(line);
                result += line;
            } else {
                result += line;
            }
            result += "\n";
        }
        if (organizer == null || result == null) {
            throw new IllegalStateException("invalid data");
        }
        return new Event(organizer, result);
    }
    
    private void handleFooter() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }
    }
    
    private String decodeLine(String line) throws IOException, DecoderException {
        if (!line.contains(QUOTED_PRINTABLE)) {
            throw new IllegalStateException("incorrect line");
        }
        String[] pieces = line.split(":");
        if (pieces.length != 2) {
            throw new IllegalArgumentException();
        }
        String[] pieces2 = pieces[0].split(";");
        String tag = pieces2[0];
        // read possibly next line(s)
        String encoded = pieces[1];
        while (encoded.endsWith("=")) {
            encoded = encoded.substring(0, encoded.length() - 1);
            encoded += reader.readLine();
        }
        return tag + ":" + decodeQuotedPrintable(encoded);
    }
    
    private String decodeQuotedPrintable(String encoded) throws UnsupportedEncodingException, DecoderException {
        String buffer = encoded.replace("=0D=0A", "\\n");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(new String(QuotedPrintableCodec.decodeQuotedPrintable(buffer.getBytes()), "UTF-8"));
        return stringBuffer.toString();
    }
    
    private String parseCn(String line) {
        if (!line.startsWith(TAG_ATTENDEE)) {
            throw new IllegalStateException("incorrect line");
        }
        // get cn from line
        String cn = line.split(";")[1].split("=")[1];
        return cn;
    }
}
