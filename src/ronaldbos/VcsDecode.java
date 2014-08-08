package ronaldbos;

import java.io.IOException;

import org.apache.commons.codec.DecoderException;

class VcsDecode {
    public static void main(String[] args) throws IOException, DecoderException {
        if (args.length != 2) {
            System.out.println("usage: program <infile.vcs> <outfile.vcs>");
            System.exit(1);
        }
        String inFileName = args[0];
        String outFileName = args[1];
        System.out.println(String.format("Decoding %s to %s", inFileName, outFileName));
        VcsDecoder decoder = new VcsDecoder();
        decoder.decode(inFileName, outFileName);
        System.out.println("Ready.");
    }
}
