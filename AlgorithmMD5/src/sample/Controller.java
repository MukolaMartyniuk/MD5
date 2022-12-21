package sample;

import com.sun.glass.ui.CommonDialogs;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    @FXML
    private TextField Text;

    @FXML
    private TextField Hash;

    @FXML
    private Button Hash_Text;

    @FXML
    private TextField File;

    @FXML
    private TextField THash_File;

    @FXML
    private Button Hash_File;

    @FXML
    private Button S_File;

    //List<String> lstFile;

    private static final int   INIT_A     = 0x67452301;
    private static final int   INIT_B     = (int) 0xEFCDAB89L;
    private static final int   INIT_C     = (int) 0x98BADCFEL;
    private static final int   INIT_D     = 0x10325476;
    private static final int[] SHIFT_AMTS = { 7, 12, 17, 22, 5, 9, 14, 20, 4,
            11, 16, 23, 6, 10, 15, 21    };
    private static final int[] TABLE_T    = new int[64];
    static
    {
        for (int i = 0; i < 64; i++)
            TABLE_T[i] = (int) (long) ((1L << 32) * Math.abs(Math.sin(i + 1)));
    }

    public String path;


    @FXML
    void initialize() {

        Hash_Text.setOnAction(actionEvent -> {
            String s = Text.getText();
            Hash.setText(toHexString(computeMD5(s.getBytes())));

        });

        S_File.setOnAction(actionEvent -> {

            FileChooser fc = new FileChooser();
            fc.setTitle("My File Chooser");
            File f = fc.showOpenDialog(null);

            if (f != null){
                File.setText(f.getAbsolutePath());
                path = f.getAbsolutePath();
            }
        });

        Hash_File.setOnAction(actionEvent -> {
            BufferedReader br = null;
            try{
                FileInputStream fin  = new FileInputStream(path);
                byte[] buffer = new byte[fin.available()];
                fin.read(buffer, 0, buffer.length);
                //System.out.println(buffer);
                fin.close();
                String Hash_Line;

                THash_File.setText(toHexString(computeMD5(buffer)));
                Hash_Line = THash_File.getText();
                File file = new File("newFile.txt");

                if (!file.exists())
                    file.createNewFile();

                PrintWriter pw = new PrintWriter(file);
                pw.println(Hash_Line);
                //System.out.println(Hash_Line);

                pw.close();

            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error: " + e);
            }

        });
    }

    public static byte[] computeMD5(byte[] message)
    {
        int messageLenBytes = message.length;
        int numBlocks = ((messageLenBytes + 8) >>> 6) + 1;
        int totalLen = numBlocks << 6;
        byte[] paddingBytes = new byte[totalLen - messageLenBytes];
        paddingBytes[0] = (byte) 0x80;
        long messageLenBits = (long) messageLenBytes << 3;
        for (int i = 0; i < 8; i++)
        {
            paddingBytes[paddingBytes.length - 8 + i] = (byte) messageLenBits;
            messageLenBits >>>= 8;
        }
        int a = INIT_A;
        int b = INIT_B;
        int c = INIT_C;
        int d = INIT_D;
        int[] buffer = new int[16];
        for (int i = 0; i < numBlocks; i++)
        {
            int index = i << 6;
            for (int j = 0; j < 64; j++, index++)
                buffer[j >>> 2] = ((int) ((index < messageLenBytes) ? message[index]
                        : paddingBytes[index - messageLenBytes]) << 24)
                        | (buffer[j >>> 2] >>> 8);
            int originalA = a;
            int originalB = b;
            int originalC = c;
            int originalD = d;
            for (int j = 0; j < 64; j++)
            {
                int div16 = j >>> 4;
                int f = 0;
                int bufferIndex = j;
                switch (div16)
                {
                    case 0:
                        f = (b & c) | (~b & d);
                        break;
                    case 1:
                        f = (b & d) | (c & ~d);
                        bufferIndex = (bufferIndex * 5 + 1) & 0x0F;
                        break;
                    case 2:
                        f = b ^ c ^ d;
                        bufferIndex = (bufferIndex * 3 + 5) & 0x0F;
                        break;
                    case 3:
                        f = c ^ (b | ~d);
                        bufferIndex = (bufferIndex * 7) & 0x0F;
                        break;
                }
                int temp = b
                        + Integer.rotateLeft(a + f + buffer[bufferIndex]
                                + TABLE_T[j],
                        SHIFT_AMTS[(div16 << 2) | (j & 3)]);
                a = d;
                d = c;
                c = b;
                b = temp;
            }
            a += originalA;
            b += originalB;
            c += originalC;
            d += originalD;
        }
        byte[] md5 = new byte[16];
        int count = 0;
        for (int i = 0; i < 4; i++)
        {
            int n = (i == 0) ? a : ((i == 1) ? b : ((i == 2) ? c : d));
            for (int j = 0; j < 4; j++)
            {
                md5[count++] = (byte) n;
                n >>>= 8;
            }
        }
        return md5;
    }

    public static String toHexString(byte[] b)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++)
        {
            sb.append(String.format("%02X", b[i] & 0xFF));
        }
        return sb.toString();
    }

}

