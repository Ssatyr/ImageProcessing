import java.io.*;
import java.util.TreeSet;
import java.util.Arrays;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
//import javax.swing.event.*;

public class Demo extends Component implements ActionListener {
    
    //************************************
    // List of the options(Original, Negative); correspond to the cases:
    //************************************

    String descs[] = {
        "Original", 
        "Negative",
        "Rescale",
        "Shift",
        "Random Shift",
        "AddSubMulDivide",
        "NOT",
        "ANDORXORROI",
        "Logarithmic",
        "Power-Low",
        "Random Look-Up Table",
        "Bit Plane Slice",
        "Histogram & Normalization",
        "Image Convolutions",
        "Salt && Pepper Noise",
        "Filtering",
        "Mean & Standard Deviation",
        "Simple Thresholding",
        "Automated Thresholding",
    };

    static JFrame f = new JFrame("Image Processing Demo");

    int opIndex;  //option index for 
    int lastOp;
    int uno_reverse_card;

    private BufferedImage bi, biFiltered, bib, bibFiltered, biba, biba2, finalbiba;   // the input image saved as bi; aka śliniaczek//
    int w, h;

    private JSlider scalingSlider; // Slider for scaling factor
    private JDialog sliderDialog; // Dialog for the slider
    private JDialog BitwiseDialog; // Dialog for the bitwise operation
    private JDialog operationDialog; // Dialog for the arithmetic operation
    private JDialog convolutionDialog; // Dialog for the convolution matrix
    private JDialog FilterDialog; // Dialog for the filter
    private JLabel sliderValueLabel; // Label to display slider value
    private int scalingFactor = 100; // Default scaling factor
    private static boolean toggle = false;
    private BufferedImage undo1;
    private BufferedImage undo2;
    
    public Demo(String imagePath) {
        try {
            File imageFile = new File(imagePath != null ? imagePath : "default.jpg");
            bi = ImageIO.read(imageFile);

            w = bi.getWidth(null);
            h = bi.getHeight(null);
            System.out.println(bi.getType());
            
            if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics big = bi2.getGraphics();
                big.drawImage(bi, 0, 0,512,512, null);
                biFiltered = bi = bi2;
            }
            initSliderDialog();
            initArithmeticOperationDialog();
            initBitwiseOperationDialog();
            initConvolutionDialog();
            initFilterDialog();
        } catch (IOException e) {      // deal with the situation that th image has problem;/
            System.out.println("Image could not be read");
            System.exit(1);
        }
    }                         

    public Dimension getPreferredSize() {
        return new Dimension(2 * w + 10, h);
    }

    String[] getDescriptions() {
        return descs;
    }

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = {"bmp","gif","jpeg","jpg","png","raw","tiff"};
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }
        return formatSet.toArray(new String[0]);
    }

    void setOpIndex(int i) {
        opIndex = i;
    }

    public void paint(Graphics g) { //  Repaint will call this function so the image will change.
        filterImage();      

        g.drawImage(biFiltered, 0, 0, null);
        g.drawImage(bi, w+10, 0, null);
        
        g.drawImage(bibFiltered, 0, bi.getHeight(null)+10, null);
        g.drawImage(bib, w+10, bi.getHeight(null)+10, null);
    }

    //************************************
    //  Convert the Buffered Image to Array
    //************************************
    private static int[][][] convertToArray(BufferedImage image){
    int width = image.getWidth();
    int height = image.getHeight();

    int[][][] result = new int[width][height][4];

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int p = image.getRGB(x,y);
            int a = (p>>24)&0xff;
            int r = (p>>16)&0xff;
            int g = (p>>8)&0xff;
            int b = p&0xff;

            result[x][y][0]=a;
            result[x][y][1]=r;
            result[x][y][2]=g;
            result[x][y][3]=b;
        }
    }
    return result;
    }

    //************************************
    //  Convert the  Array to BufferedImage
    //************************************
    public BufferedImage convertToBimage(int[][][] TmpArray){

        int width = TmpArray.length;
        int height = TmpArray[0].length;

        BufferedImage tmpimg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                int a = TmpArray[x][y][0];
                int r = TmpArray[x][y][1];
                int g = TmpArray[x][y][2];
                int b = TmpArray[x][y][3];
                
                //set RGB value

                int p = (a<<24) | (r<<16) | (g<<8) | b;
                tmpimg.setRGB(x, y, p);

            }
        }
        return tmpimg;
    }


    //************************************
    //  Example:  Image Negative
    //************************************
    public BufferedImage ImageNegative(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = 255-ImageArray[x][y][1];  //r
                ImageArray[x][y][2] = 255-ImageArray[x][y][2];  //g
                ImageArray[x][y][3] = 255-ImageArray[x][y][3];  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage ReScaleImage(BufferedImage timg, int scalingFactor){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        float scalingFactorFloat = (float)scalingFactor/100;
        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = Math.max(Math.min(Math.round(ImageArray[x][y][1]  * scalingFactorFloat), 255), 0);  //r
                ImageArray[x][y][2] = Math.max(Math.min(Math.round(ImageArray[x][y][2]  * scalingFactorFloat), 255), 0);  //g
                ImageArray[x][y][3] = Math.max(Math.min(Math.round(ImageArray[x][y][3]  * scalingFactorFloat), 255), 0);  //b

            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage Shift(BufferedImage timg, int scalingFactor){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = Math.max(Math.min(Math.round(ImageArray[x][y][1]  + scalingFactor), 255), 0);  //r
                ImageArray[x][y][2] = Math.max(Math.min(Math.round(ImageArray[x][y][2]  + scalingFactor), 255), 0);  //g
                ImageArray[x][y][3] = Math.max(Math.min(Math.round(ImageArray[x][y][3]  + scalingFactor), 255), 0);  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage RandomShift(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                int random = (int)(Math.random() * 511 - 256);
                ImageArray[x][y][1] = Math.max(Math.min(Math.round(ImageArray[x][y][1]  + random), 255), 0);  //r
                ImageArray[x][y][2] = Math.max(Math.min(Math.round(ImageArray[x][y][2]  + random), 255), 0);  //g
                ImageArray[x][y][3] = Math.max(Math.min(Math.round(ImageArray[x][y][3]  + random), 255), 0);  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage Addition(BufferedImage timg, BufferedImage timg2){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        int[][][] ImageArray2 = convertToArray(timg2);          //  Convert the image to array

        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = Math.max(Math.min(ImageArray[x][y][1]  + ImageArray2[x][y][1], 255), 0);  //r
                ImageArray[x][y][2] = Math.max(Math.min(ImageArray[x][y][2]  + ImageArray2[x][y][2], 255), 0);  //g
                ImageArray[x][y][3] = Math.max(Math.min(ImageArray[x][y][3]  + ImageArray2[x][y][3], 255), 0);  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage Subtraction(BufferedImage timg, BufferedImage timg2){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        int[][][] ImageArray2 = convertToArray(timg2);          //  Convert the image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = Math.max(Math.min(ImageArray[x][y][1]  - ImageArray2[x][y][1], 255), 0);  //r
                ImageArray[x][y][2] = Math.max(Math.min(ImageArray[x][y][2]  - ImageArray2[x][y][2], 255), 0);  //g
                ImageArray[x][y][3] = Math.max(Math.min(ImageArray[x][y][3]  - ImageArray2[x][y][3], 255), 0);  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage Multiplication(BufferedImage timg, BufferedImage timg2){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        int[][][] ImageArray2 = convertToArray(timg2);          //  Convert the image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = Math.max(Math.min(Math.round(ImageArray[x][y][1]  * ImageArray2[x][y][1] / 255), 255), 0);  //r
                ImageArray[x][y][2] = Math.max(Math.min(Math.round(ImageArray[x][y][2]  * ImageArray2[x][y][2] / 255), 255), 0);  //g
                ImageArray[x][y][3] = Math.max(Math.min(Math.round(ImageArray[x][y][3]  * ImageArray2[x][y][3] / 255), 255), 0);  //b
                // czy ma byc skalowane /255 czy po prostu mnożone?
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage Dividing(BufferedImage timg, BufferedImage timg2){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        int[][][] ImageArray2 = convertToArray(timg2);          //  Convert the image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                if (ImageArray2[x][y][1] == 0) ImageArray2[x][y][1] = 1;
                if (ImageArray2[x][y][2] == 0) ImageArray2[x][y][2] = 1;
                if (ImageArray2[x][y][3] == 0) ImageArray2[x][y][3] = 1;
                ImageArray[x][y][1] = Math.min(Math.round((ImageArray[x][y][1]  / ImageArray2[x][y][1]) * 255), 255);  //r
                ImageArray[x][y][2] = Math.min(Math.round((ImageArray[x][y][2]  / ImageArray2[x][y][2]) * 255), 255);  //g
                ImageArray[x][y][3] = Math.min(Math.round((ImageArray[x][y][3]  / ImageArray2[x][y][3]) * 255), 255);  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage BitwiseNOT(BufferedImage timg) {
        int width = timg.getWidth();
        int height = timg.getHeight();
        BufferedImage resultImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = timg.getRGB(x, y);
                int a = (p>>24) & 0xff; // Extract the alpha channel
                int r = (p>>16) & 0xff; // Extract the red channel
                int g = (p>>8) & 0xff;  // Extract the green channel
                int b = p & 0xff;       // Extract the blue channel
    
                // Apply NOT operation only to the color channels
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
    
                // Reassemble the pixel with the original alpha channel
                p = (a<<24) | (r<<16) | (g<<8) | b;
                resultImg.setRGB(x, y, p);
            }
        }
        return resultImg;
    }

    public BufferedImage BitwiseAND(BufferedImage timg, BufferedImage timg2){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        int[][][] ImageArray2 = convertToArray(timg2);          //  Convert the image to array

        //Image Bitwise AND Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = ImageArray[x][y][1] & ImageArray2[x][y][1];  //r
                ImageArray[x][y][2] = ImageArray[x][y][2] & ImageArray2[x][y][2];  //g
                ImageArray[x][y][3] = ImageArray[x][y][3] & ImageArray2[x][y][3];  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage BitwiseOR(BufferedImage timg, BufferedImage timg2){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        int[][][] ImageArray2 = convertToArray(timg2);          //  Convert the image to array

        //Image Bitwise OR Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = ImageArray[x][y][1] &0xFF | ImageArray2[x][y][1] &0xFF;  //r
                ImageArray[x][y][2] = ImageArray[x][y][2] &0xFF | ImageArray2[x][y][2] &0xFF;  //g
                ImageArray[x][y][3] = ImageArray[x][y][3] &0xFF | ImageArray2[x][y][3] &0xFF;  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage BitwiseXOR(BufferedImage timg, BufferedImage timg2){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        int[][][] ImageArray2 = convertToArray(timg2);          //  Convert the image to array

        //Image Bitwise XOR Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = ImageArray[x][y][1] ^ ImageArray2[x][y][1];  //r
                ImageArray[x][y][2] = ImageArray[x][y][2] ^ ImageArray2[x][y][2];  //g
                ImageArray[x][y][3] = ImageArray[x][y][3] ^ ImageArray2[x][y][3];  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage applyROI(BufferedImage targetImg, BufferedImage maskImg) {
        // Assuming both images are the same size
        int width = targetImg.getWidth();
        int height = targetImg.getHeight();
    
        BufferedImage resultImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
        // Convert the mask to alpha values (0 for non-ROI, 255 for ROI)
        BufferedImage bwImage = thresholdImage(maskImg, 127);
        int[][][] maskArray = convertToArray(bwImage);
        int[][][] targetArray = convertToArray(targetImg);
    
        int whiteCount = 0;
        int blackCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int maskValue = maskArray[x][y][1]; // Using red channel as example; mask should be grayscale
                float alpha = (maskValue == 255) ? 1.0f : 0.0f; // Convert to 1 for white and 0 for black

                if (alpha == 1.0f) whiteCount++;
                else blackCount++;

                // Apply the ROI mask to each color channel
                int a = 255; // Alpha channel is opaque
                int r = (int)(targetArray[x][y][1] * alpha);
                int g = (int)(targetArray[x][y][2] * alpha);
                int b = (int)(targetArray[x][y][3] * alpha);

                // Combine channels back to an int and set it to the result image
                int p = (a<<24) | (r<<16) | (g<<8) | b;
                resultImg.setRGB(x, y, p);
            }
        }

        System.out.println("White pixels in mask: " + whiteCount);
        System.out.println("Black pixels in mask: " + blackCount);

        return resultImg;
    }

    public BufferedImage thresholdImage(BufferedImage srcImage, int threshold) {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        
        // Create a new BufferedImage to hold the thresholded image
        BufferedImage thresholdedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the current pixel value (assuming a grayscale image)
                int p = srcImage.getRGB(x, y);
                int a = (p>>24)&0xff; // alpha channel
                int r = (p>>16)&0xff; // red channel
                int g = (p>>8)&0xff;  // green channel
                int b = p&0xff;       // blue channel
    
                // Convert to grayscale using a simple average
                int avg = (r + g + b) / 3;
    
                // Apply threshold
                if (avg > threshold) {
                    p = (a<<24) | (255<<16) | (255<<8) | 255; // Set to white
                } else {
                    p = (a<<24) | (0<<16) | (0<<8) | 0;       // Set to black
                }
    
                thresholdedImage.setRGB(x, y, p);
            }
        }
        
        return thresholdedImage;
    }
    
    public BufferedImage LogFunction(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        // Image Logarithmic Function
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = (int)(255 * Math.log(1 + ImageArray[x][y][1])/Math.log(256));  //r
                ImageArray[x][y][2] = (int)(255 * Math.log(1 + ImageArray[x][y][2])/Math.log(256));  //g
                ImageArray[x][y][3] = (int)(255 * Math.log(1 + ImageArray[x][y][3])/Math.log(256));  //b
            }
        }

        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage PowerLow (BufferedImage timg, int scalingFactor){
        int width = timg.getWidth();
        int height = timg.getHeight();

        float scalingFactorFloat = (float)scalingFactor/100;

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
        double c  = (Math.pow(255, 1 - scalingFactorFloat));
        // Image Power-Law Function
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = (int)(c * Math.pow(ImageArray[x][y][1], scalingFactorFloat));  //r
                ImageArray[x][y][2] = (int)(c * Math.pow(ImageArray[x][y][2], scalingFactorFloat));  //g
                ImageArray[x][y][3] = (int)(c * Math.pow(ImageArray[x][y][3], scalingFactorFloat));  //b
            }
        }

        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage RandomLookUpTable(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = (int)(Math.random() * 256);
        }

        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = lut[ImageArray[x][y][1]];  //r
                ImageArray[x][y][2] = lut[ImageArray[x][y][2]];  //g
                ImageArray[x][y][3] = lut[ImageArray[x][y][3]];  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }
    
    public BufferedImage BitPlaneSlice(BufferedImage timg, int scalingFactor){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = ((ImageArray[x][y][1] >> scalingFactor) & 1) * 255;  //r
                ImageArray[x][y][2] = ((ImageArray[x][y][2] >> scalingFactor) & 1) * 255;  //g
                ImageArray[x][y][3] = ((ImageArray[x][y][3] >> scalingFactor) & 1) * 255;  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public BufferedImage FindingHistogram(BufferedImage timg){

        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg); 

        int[] histogramR = new int[256];
        int[] histogramG = new int[256];
        int[] histogramB = new int[256];

        // Finding Histogram

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                histogramR[ImageArray[x][y][1]]++;  //r
                histogramG[ImageArray[x][y][2]]++;  //g
                histogramB[ImageArray[x][y][3]]++;  //b
            }
        }

        int[] cdfR = computeCDF(histogramR);
        int[] cdfG = computeCDF(histogramG);
        int[] cdfB = computeCDF(histogramB);

        // Normalization
        for (int i = 0; i < 256; i++) {
            histogramR[i] = (int)(255 * (histogramR[i] / (double)(w * h)));
            histogramG[i] = (int)(255 * (histogramG[i] / (double)(w * h)));
            histogramB[i] = (int)(255 * (histogramB[i] / (double)(w * h)));
        }

        //Equalisation
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = cdfR[ImageArray[x][y][1]];  //r
                ImageArray[x][y][2] = cdfG[ImageArray[x][y][2]];  //g
                ImageArray[x][y][3] = cdfB[ImageArray[x][y][3]];  //b
            }
        }

        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    public int[] computeCDF(int[] histogram) {
        int[] cdf = new int[256];
        int cum = 0;
        int minValue = 0;
        int size = histogram.length;
    
        for (int i = 0; i < size; i++) {
            cum += histogram[i];
            cdf[i] = cum;
            if (minValue == 0 && cum > 0) {
                minValue = cum;
            }
        }
    
        // Normalize CDF to 0-255 range
        for (int i = 0; i < size; i++) {
            cdf[i] = Math.round(((cdf[i] - minValue) / (float)(cum - minValue)) * 255);
        }
    
        return cdf;
    }

    public BufferedImage Convolution(BufferedImage timg, int[][] kernel, int avg){
        int width = timg.getWidth();
        int height = timg.getHeight();
        System.out.println(kernel[0][0] + ", " + kernel[0][1] + ", " + kernel[0][2]);
        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        int kernelSize = 3;
        int kernelSum = 0;

        for (int y = 0; y < kernelSize; y++) {
            for (int x = 0; x < kernelSize; x++) {
                kernelSum += kernel[y][x];
            }
        }

        int[][][] resultArray = new int[width][height][4];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumR = 0;
                int sumG = 0;
                int sumB = 0;
        
                for (int ky = 0; ky < kernelSize; ky++) {
                    for (int kx = 0; kx < kernelSize; kx++) {
                        int pixelR = ImageArray[x + kx - 1][y + ky - 1][1];
                        int pixelG = ImageArray[x + kx - 1][y + ky - 1][2];
                        int pixelB = ImageArray[x + kx - 1][y + ky - 1][3];
                        sumR += kernel[kernelSize - 1 - ky][kernelSize - 1 - kx] * pixelR;
                        sumG += kernel[kernelSize - 1 - ky][kernelSize - 1 - kx] * pixelG;
                        sumB += kernel[kernelSize - 1 - ky][kernelSize - 1 - kx] * pixelB;
                    }
                }
                
                if(avg ==1)
                {
                    if (kernelSum != 0) {
                        resultArray[x][y][1] = Math.abs(sumR) / kernelSum;
                        resultArray[x][y][2] = Math.abs(sumG) / kernelSum;
                        resultArray[x][y][3] = Math.abs(sumB) / kernelSum;
                    } else { 
                        resultArray[x][y][1] = Math.abs(sumR);
                        resultArray[x][y][2] = Math.abs(sumG);
                        resultArray[x][y][3] = Math.abs(sumB);
                    }
                }
                else
                {
                    resultArray[x][y][1] = Math.abs(sumR); 
                    resultArray[x][y][2] = Math.abs(sumG); 
                    resultArray[x][y][3] = Math.abs(sumB);
                }
            }
        }

        return convertToBimage(resultArray);  // Convert the array to BufferedImage
    }

    public BufferedImage applyRobertsOperator(BufferedImage timg) {
        int[][] kernelX = {
            {0, 0, 0},
            {0, 0, -1},
            {0, 1, 0}
        };
    
        int[][] kernelY = {
            {0, 0, 0},
            {0, -1, 0},
            {0, 0, 1}
        };
    
        BufferedImage resultX = Convolution(timg, kernelX, 0);
        BufferedImage resultY = Convolution(timg, kernelY, 0);
    
        int width = timg.getWidth();
        int height = timg.getHeight();
        int[][][] ImageArrayX = convertToArray(resultX);
        int[][][] ImageArrayY = convertToArray(resultY);
    
        int[][][] resultArray = new int[width][height][4];
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sumR = (int)Math.sqrt(Math.pow(ImageArrayX[x][y][1], 2) + Math.pow(ImageArrayY[x][y][1], 2));
                int sumG = (int)Math.sqrt(Math.pow(ImageArrayX[x][y][2], 2) + Math.pow(ImageArrayY[x][y][2], 2));
                int sumB = (int)Math.sqrt(Math.pow(ImageArrayX[x][y][3], 2) + Math.pow(ImageArrayY[x][y][3], 2));
    
                resultArray[x][y][1] = Math.min(sumR, 255);
                resultArray[x][y][2] = Math.min(sumG, 255);
                resultArray[x][y][3] = Math.min(sumB, 255);
            }
        }
    
        return convertToBimage(resultArray);
    }

    public BufferedImage Convolutions5by5(BufferedImage timg, int[][] kernel){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          

        int kernelSize = 5;
        int kernelSum = 0;

        for (int y = 0; y < kernelSize; y++) {
            for (int x = 0; x < kernelSize; x++) {
                kernelSum += kernel[y][x];
            }
        }

        int[][][] resultArray = new int[width][height][4];

        for (int y = 2; y < height - 2; y++) {
            for (int x = 2; x < width - 2; x++) {
                int sumR = 0;
                int sumG = 0;
                int sumB = 0;
        
                for (int ky = 0; ky < kernelSize; ky++) {
                    for (int kx = 0; kx < kernelSize; kx++) {
                        int pixelR = ImageArray[x + kx - 2][y + ky - 2][1];
                        int pixelG = ImageArray[x + kx - 2][y + ky - 2][2];
                        int pixelB = ImageArray[x + kx - 2][y + ky - 2][3];
                        sumR += kernel[ky][kx] * pixelR;
                        sumG += kernel[ky][kx] * pixelG;
                        sumB += kernel[ky][kx] * pixelB;
                    }
                }
        
                if (kernelSum != 0) { // Avoid division by zero
                    resultArray[x][y][1] = Math.abs(sumR) / kernelSum;
                    resultArray[x][y][2] = Math.abs(sumG) / kernelSum;
                    resultArray[x][y][3] = Math.abs(sumB) / kernelSum;
                } else { // In case kernelSum is 0, assign the absolute values directly
                    resultArray[x][y][1] = Math.abs(sumR);
                    resultArray[x][y][2] = Math.abs(sumG);
                    resultArray[x][y][3] = Math.abs(sumB);
                }
            }
        }

        return convertToBimage(resultArray);

    }

    public BufferedImage SaltandPepper (BufferedImage timg){


        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        // salt and perpper noise  
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                int random = (int)(Math.random() * 100);
                if(random < 5){
                    ImageArray[x][y][1] = 0;  //r
                    ImageArray[x][y][2] = 0;  //g
                    ImageArray[x][y][3] = 0;  //b
                } else if(random > 95){
                    ImageArray[x][y][1] = 255;  //r
                    ImageArray[x][y][2] = 255;  //g
                    ImageArray[x][y][3] = 255;  //b
                }
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage  
    }

    public BufferedImage MinFilter (BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        int[][][] resultArray = new int[width][height][4];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int minR = 255;
                int minG = 255;
                int minB = 255;
        
                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        int pixelR = ImageArray[x + kx - 1][y + ky - 1][1];
                        int pixelG = ImageArray[x + kx - 1][y + ky - 1][2];
                        int pixelB = ImageArray[x + kx - 1][y + ky - 1][3];
                        if (pixelR < minR) minR = pixelR;
                        if (pixelG < minG) minG = pixelG;
                        if (pixelB < minB) minB = pixelB;
                    }
                }
        
                resultArray[x][y][1] = minR;
                resultArray[x][y][2] = minG;
                resultArray[x][y][3] = minB;
            }
        }
    
        return convertToBimage(resultArray);
    }

    public BufferedImage MaxFilter (BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        int[][][] resultArray = new int[width][height][4];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int maxR = 0;
                int maxG = 0;
                int maxB = 0;
        
                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        int pixelR = ImageArray[x + kx - 1][y + ky - 1][1];
                        int pixelG = ImageArray[x + kx - 1][y + ky - 1][2];
                        int pixelB = ImageArray[x + kx - 1][y + ky - 1][3];
                        if (pixelR > maxR) maxR = pixelR;
                        if (pixelG > maxG) maxG = pixelG;
                        if (pixelB > maxB) maxB = pixelB;
                    }
                }
        
                resultArray[x][y][1] = maxR;
                resultArray[x][y][2] = maxG;
                resultArray[x][y][3] = maxB;
            }
        }
    
        return convertToBimage(resultArray);
    }

    public BufferedImage MedianFilter (BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        int[][][] resultArray = new int[width][height][4];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] red = new int[9];
                int[] green = new int[9];
                int[] blue = new int[9];
        
                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        int pixelR = ImageArray[x + kx - 1][y + ky - 1][1];
                        int pixelG = ImageArray[x + kx - 1][y + ky - 1][2];
                        int pixelB = ImageArray[x + kx - 1][y + ky - 1][3];
                        red[ky * 3 + kx] = pixelR;
                        green[ky * 3 + kx] = pixelG;
                        blue[ky * 3 + kx] = pixelB;
                    }
                }

                Arrays.sort(red);
                Arrays.sort(green);
                Arrays.sort(blue);
        
                resultArray[x][y][1] = red[4];
                resultArray[x][y][2] = green[4];
                resultArray[x][y][3] = blue[4];
            }
        }
    
        return convertToBimage(resultArray);
    }

    public BufferedImage MidpointFilter (BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        int[][][] resultArray = new int[width][height][4];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] red = new int[9];
                int[] green = new int[9];
                int[] blue = new int[9];
        
                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        int pixelR = ImageArray[x + kx - 1][y + ky - 1][1];
                        int pixelG = ImageArray[x + kx - 1][y + ky - 1][2];
                        int pixelB = ImageArray[x + kx - 1][y + ky - 1][3];
                        red[ky * 3 + kx] = pixelR;
                        green[ky * 3 + kx] = pixelG;
                        blue[ky * 3 + kx] = pixelB;
                    }
                }
        
                Arrays.sort(red);
                Arrays.sort(green);
                Arrays.sort(blue);
        
                resultArray[x][y][1] = (red[0] + red[8]) / 2;
                resultArray[x][y][2] = (green[0] + green[8]) / 2;
                resultArray[x][y][3] = (blue[0] + blue[8]) / 2;
            }
        }
    
        return convertToBimage(resultArray);
    }

    public void MeanandStd(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        int[] red = new int[width * height];
        int[] green = new int[width * height];
        int[] blue = new int[width * height];

        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                red[count] = ImageArray[x][y][1];
                green[count] = ImageArray[x][y][2];
                blue[count] = ImageArray[x][y][3];
                count++;
            }
        }

        double meanR = mean(red);
        double meanG = mean(green);
        double meanB = mean(blue);

        double stdR = std(red, meanR);
        double stdG = std(green, meanG);
        double stdB = std(blue, meanB);

        System.out.println("Mean of Red: " + meanR);
        System.out.println("Mean of Green: " + meanG);
        System.out.println("Mean of Blue: " + meanB);
        System.out.println("Standard Deviation of Red: " + stdR);
        System.out.println("Standard Deviation of Green: " + stdG);
        System.out.println("Standard Deviation of Blue: " + stdB);
    }

    // Calculate the mean of an array
    public double mean(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return (double) sum / array.length;
    }
    public double std(int[] array, double mean) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += Math.pow(array[i] - mean, 2);
        }
        double variance = sum / array.length;
        return Math.sqrt(variance);
    }

    public BufferedImage SimpleThresholding(BufferedImage timg, int threshold){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          

        // Simple Thresholding
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                if(ImageArray[x][y][1] > threshold){
                    ImageArray[x][y][1] = 255;  //r
                } else {
                    ImageArray[x][y][1] = 0;  //r
                }
                if(ImageArray[x][y][2] > threshold){
                    ImageArray[x][y][2] = 255;  //g
                } else {
                    ImageArray[x][y][2] = 0;  //g
                }
                if(ImageArray[x][y][3] > threshold){
                    ImageArray[x][y][3] = 255;  //b
                } else {
                    ImageArray[x][y][3] = 0;  //b
                }
            }
        }
        
        return convertToBimage(ImageArray);  
    }

    public BufferedImage AutomatedThresholding(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          

        int[] histogramR = new int[256];
        int[] histogramG = new int[256];
        int[] histogramB = new int[256];

        // Finding Histogram
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                histogramR[ImageArray[x][y][1]]++;  //r
                histogramG[ImageArray[x][y][2]]++;  //g
                histogramB[ImageArray[x][y][3]]++;  //b
            }
        }

        int thresholdR = otsuThreshold(histogramR); 
        int thresholdG = otsuThreshold(histogramG); 
        int thresholdB = otsuThreshold(histogramB);

        // Automated Thresholding
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                if(ImageArray[x][y][1] > thresholdR){
                    ImageArray[x][y][1] = 255;  //r
                } else {
                    ImageArray[x][y][1] = 0;  //r
                }
                if(ImageArray[x][y][2] > thresholdG){
                    ImageArray[x][y][2] = 255;  //g
                } else {
                    ImageArray[x][y][2] = 0;  //g
                }
                if(ImageArray[x][y][3] > thresholdB){
                    ImageArray[x][y][3] = 255;  //b
                } else {
                    ImageArray[x][y][3] = 0;  //b
                }
            }
        }
        
        return convertToBimage(ImageArray);  
    }

    public int otsuThreshold(int[] histogram) { 
        int total = 0;
        for (int i = 0; i < histogram.length; i++) {
            total += histogram[i];
        }
    
        float sum = 0;
        for (int i = 0; i < histogram.length; i++) {
            sum += i * histogram[i];
        }
    
        float sumB = 0; // Sum Background
        int wB = 0; // Weight Background
        int wF = 0; // Weight Foreground
    
        float varMax = 0;
        int threshold = 0;
    
        for (int i = 0; i < histogram.length; i++) {
            wB += histogram[i]; 
            if (wB == 0) {
                continue;
            }
            wF = total - wB;
    
            if (wF == 0) {
                break;
            }
            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
    
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
    
        return threshold;
    }


    //************************************
    //  You need to register your functioin here
    //************************************
    public void filterImage() {
        
        if (opIndex != 15 && opIndex != 13) {
            undo1 = biFiltered;
            if (bib != null) {
                undo2 = bibFiltered;
            }
        }
        
        if (opIndex == lastOp && toggle == false) {
            return;
        }

        BufferedImage inputImage = toggle ? biFiltered : bi;
        BufferedImage inputImageB = toggle ? bibFiltered : bib;

        uno_reverse_card = lastOp;
        lastOp = opIndex;

        switch (opIndex) {
        case 0: biFiltered = bi; /* original */
                bibFiltered = bib;
                return; 
        case 1: biFiltered = ImageNegative(inputImage); /* Image Negative */
                if(bib != null){
                    bibFiltered = ImageNegative(inputImageB);
                }
                return;
        case 2: 
                biFiltered = ReScaleImage(inputImage, scalingFactor); /* Image Rescaling */
                if(bib != null){
                    bibFiltered = ReScaleImage(inputImageB, scalingFactor);
                }
                return;
        case 3: 
                biFiltered = Shift(inputImage, scalingFactor);
                if(bib != null){
                    bibFiltered = Shift(inputImageB, scalingFactor);
                }
                return;
        case 4:
                biFiltered = RandomShift(inputImage);
                if(bib != null){
                    bibFiltered = RandomShift(inputImageB);
                }
                return;
        case 6:
                biFiltered = BitwiseNOT(inputImage);
                if(bib != null){
                    bibFiltered = BitwiseNOT(inputImageB);
                }
                return;
        case 8:
                biFiltered = LogFunction(inputImage);
                if(bib != null){
                    bibFiltered = LogFunction(inputImageB);
                }
                return;
        case 9:
                biFiltered = PowerLow(inputImage, scalingFactor);
                if(bib != null){
                    bibFiltered = PowerLow(inputImageB, scalingFactor);
                }
                return;
        case 10:
                biFiltered = RandomLookUpTable(inputImage);
                if(bib != null){
                    bibFiltered = RandomLookUpTable(inputImageB);
                }
                return;
        case 11:
                biFiltered = BitPlaneSlice(inputImage, scalingFactor);
                if(bib != null){
                    bibFiltered = BitPlaneSlice(inputImageB, scalingFactor);
                }
                return;
        case 12:
                biFiltered = FindingHistogram(inputImage);
                if(bib != null){
                    bibFiltered = FindingHistogram(inputImageB);
                }
                return;
        case 14:
                biFiltered = SaltandPepper(inputImage);
                if(bib != null){
                    bibFiltered = SaltandPepper(inputImageB);
                }
                return;
        case 16:
                MeanandStd(inputImage);
                if(bib != null){
                    MeanandStd(inputImageB);
                }
                return;
        case 17:
                biFiltered = SimpleThresholding(inputImage, scalingFactor);
                if(bib != null){
                    bibFiltered = SimpleThresholding(inputImageB, scalingFactor);
                }
                return;
        case 18:
                biFiltered = AutomatedThresholding(inputImage);
                if(bib != null){
                    bibFiltered = AutomatedThresholding(inputImageB);
                }
                return;
        }
    }

    public void initConvolutionDialog(){
        convolutionDialog = new JDialog((Frame) null, "Adjust Convolution Matrix", true); // Make it modal
        convolutionDialog.setLayout(new FlowLayout());
        convolutionDialog.setSize(300, 150); // Or adjust size as needed

        String[] operations = {"Averaging", "Weighted Averaging", "4-n Laplacian", "8-n Laplacian", "4-n Laplacian Enhanced", "8-n Laplacian Enhanced", "Roberts", "Sobel X", "Sobel Y", "Gaussian", "LaPlacian of Gaussian"};
        JComboBox<String> operationList = new JComboBox<>(operations);

        JButton applyConvolution = new JButton("Apply Convolution");
        applyConvolution.addActionListener(e -> {
            String operation = (String) operationList.getSelectedItem();
            System.out.println("Operation: " + operation);  
            BufferedImage inputImage = toggle ? biFiltered : bi;
            BufferedImage inputImageB = toggle ? bibFiltered : bib;
            undo1 = biFiltered;
            if(bib != null){
                undo2 = bibFiltered;
            }
            switch (operation) {
                case "Averaging":
                    int[][] kernel = {{1,1,1},{1,1,1},{1,1,1}};
                    biFiltered = Convolution(inputImage, kernel, 1);
                    if(bib != null){
                        bibFiltered = Convolution(inputImageB, kernel, 1);
                    }
                    break;
                case "Weighted Averaging":
                    int[][] weightedKernel = {{1,2,1},{2,4,2},{1,2,1}};
                    biFiltered = Convolution(inputImage, weightedKernel, 1);
                    if(bib != null){
                        bibFiltered = Convolution(inputImageB, weightedKernel, 1);
                    }
                    break;
                case "4-n Laplacian":
                    System.out.println("4-n Laplacian");
                    int[][] laplacian4 = {{0,-1,0},{-1,4,-1},{0,-1,0}};
                    biFiltered = Convolution(inputImage, laplacian4, 0);
                    if(bib != null){
                        bibFiltered = Convolution(inputImageB, laplacian4, 0);
                    }
                    break; 
                case "8-n Laplacian":
                    int[][] laplacian8 = {{-1,-1,-1},{-1,8,-1},{-1,-1,-1}};
                    biFiltered = Convolution(inputImage, laplacian8, 0);
                    if(bib != null){
                        bibFiltered = Convolution(inputImageB, laplacian8, 0);
                    }
                    break;
                case "4-n Laplacian Enhanced":
                    int[][] laplacian4Enhanced = {{0,-1,0},{-1,5,-1},{0,-1,0}};
                    biFiltered = Convolution(inputImage, laplacian4Enhanced, 0);
                    if(bib != null){
                        bibFiltered = Convolution(inputImageB, laplacian4Enhanced, 0);
                    }
                    break;
                case "8-n Laplacian Enhanced":
                    int[][] laplacian8Enhanced = {{-1,-1,-1},{-1,9,-1},{-1,-1,-1}};
                    biFiltered = Convolution(inputImage, laplacian8Enhanced, 0);
                    if(bib != null){
                        bibFiltered = Convolution(inputImageB, laplacian8Enhanced, 0);
                    }
                    break;
                case "Roberts":
                    biFiltered = applyRobertsOperator(inputImage);
                    if(bib != null){
                        bibFiltered = applyRobertsOperator(inputImageB);
                    }
                    break;
                case "Sobel X":
                    int[][] sobelX = {{-1,0,1},{-2,0,2},{-1,0,1}};
                    biFiltered = Convolution(inputImage, sobelX, 0);
                    if(bib != null){
                        bibFiltered = Convolution(inputImageB, sobelX, 0);
                    }     
                    break;
                case "Sobel Y":
                    int[][] sobelY = {{-1,-2,-1},{0,0,0},{1,2,1}};
                    biFiltered = Convolution(inputImage, sobelY, 0);
                    if(bib != null){
                        bibFiltered = Convolution(inputImageB, sobelY, 0);
                    }
                    break;
                case "Gaussian":
                    int [][] gaussian = {{1, 4, 7, 4, 1}, {4, 16, 26, 16, 4}, {7, 26, 41, 26, 7}, {4, 16, 26, 16, 4}, {1, 4, 7, 4, 1}};
                    biFiltered = Convolutions5by5(inputImage, gaussian);
                    if(bib != null){
                        bibFiltered = Convolutions5by5(inputImageB, gaussian);
                    }
                    break;
                case "LaPlacian of Gaussian":
                    int [][] laplacianOfGaussian = {{0, 0, -1, 0, 0}, {0, -1, -2, -1, 0}, {-1, -2, 16, -2, -1}, {0, -1, -2, -1, 0}, {0, 0, -1, 0, 0}};
                    biFiltered = Convolutions5by5(inputImage, laplacianOfGaussian);
                    if(bib != null){
                        bibFiltered = Convolutions5by5(inputImageB, laplacianOfGaussian);
                    }
                    break;
            }
            repaint();
            convolutionDialog.setVisible(false);
        });
        convolutionDialog.add(operationList);
        convolutionDialog.add(applyConvolution);
        convolutionDialog.setLocationRelativeTo(f); // Center dialog relative to the frame
    }

    public void initFilterDialog(){
        FilterDialog = new JDialog((Frame) null, "Choose filter to apply", true); // Make it modal
        FilterDialog.setLayout(new FlowLayout());
        FilterDialog.setSize(300, 150); // Or adjust size as needed

        String[] operations = {"Min Filter", "Max Filter", "Median Filter","Midpoint Filter"};
        JComboBox<String> operationList = new JComboBox<>(operations);

        JButton applyConvolution = new JButton("Apply Filter");
        applyConvolution.addActionListener(e -> {
            String operation = (String) operationList.getSelectedItem();
            BufferedImage inputImage = toggle ? biFiltered : bi;
            BufferedImage inputImageB = toggle ? bibFiltered : bib;
            undo1 = biFiltered;
            if(bib != null){
                undo2 = bibFiltered;
            }
            switch (operation) {
                case "Min Filter":
                    biFiltered = MinFilter(inputImage);
                    if(bib != null){
                        bibFiltered = MinFilter(inputImageB);
                    }
                    break;
                case "Max Filter":
                    biFiltered = MaxFilter(inputImage);
                    if(bib != null){
                        bibFiltered = MaxFilter(inputImageB);
                    }
                    break;
                case "Median Filter":
                    biFiltered = MedianFilter(inputImage);
                    if(bib != null){
                        bibFiltered = MedianFilter(inputImageB);
                    }
                    break;
                case "Midpoint Filter":
                    biFiltered = MidpointFilter(inputImage);
                    if(bib != null){
                        bibFiltered = MidpointFilter(inputImageB);
                    }
                    break;
            }
            repaint();
            FilterDialog.setVisible(false);
        });
        FilterDialog.add(operationList);
        FilterDialog.add(applyConvolution);
        FilterDialog.setLocationRelativeTo(f); // Center dialog relative to the frame
    }

    public void initSliderDialog(){
        // Initialize the dialog for the scaling factor with no owner frame, making it a top-level window
        sliderDialog = new JDialog((Frame) null, "Adjust Scaling Factor ", true); // Make it modal
        sliderDialog.setLayout(new FlowLayout());
        sliderDialog.setSize(300, 150); // Or adjust size as needed
    
        // Initialize the slider
        scalingSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
        scalingSlider.setMajorTickSpacing(50);
        scalingSlider.setPaintTicks(true);
        scalingSlider.setPaintLabels(true);
    
        // Label to display the current value of the slider
        sliderValueLabel = new JLabel("Value: " + scalingSlider.getValue());
        scalingSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            scalingFactor = source.getValue();
            sliderValueLabel.setText("Value: " + scalingFactor);
        });
    
        // Button to apply changes
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            repaint();
            sliderDialog.setVisible(false); // Hide dialog after applying changes
        });
    
        // Add components to the dialog 
        sliderDialog.add(scalingSlider);
        sliderDialog.add(sliderValueLabel);
        sliderDialog.add(applyButton);
        sliderDialog.setLocationRelativeTo(f); // Center dialog relative to the frame
    }

    public void updateSliderForOperation() {
        if (Integer.valueOf(opIndex).equals(3)) {
            scalingSlider.setMinimum(-255);
            scalingSlider.setMaximum(255);
            scalingSlider.setValue(0); // Default value for shifting
        } else if (Integer.valueOf(opIndex).equals(2)) {
            scalingSlider.setMinimum(0);
            scalingSlider.setMaximum(200);
            scalingSlider.setValue(100); // Default scaling factor
        } else if (Integer.valueOf(opIndex).equals(9)){
            scalingSlider.setMinimum(1);
            scalingSlider.setMaximum(2500);
            scalingSlider.setValue(100); // Default scaling factor
        } else if (Integer.valueOf(opIndex).equals(11)){
            scalingSlider.setMinimum(0);
            scalingSlider.setMaximum(7);
            scalingSlider.setValue(3); // Default scaling factor
        } else if(Integer.valueOf(opIndex).equals(17)){
            scalingSlider.setMinimum(0);
            scalingSlider.setMaximum(255);
            scalingSlider.setValue(0); // Default scaling factor
        } 
        sliderValueLabel.setText("Value: " + scalingSlider.getValue());
    }

    public void initArithmeticOperationDialog(){
        operationDialog = new JDialog((Frame) null, "Adjust Arithmetic Operation", true); // Make it modal
        operationDialog.setLayout(new FlowLayout());
        operationDialog.setSize(800, 150); // Or adjust size as needed
        JButton chooseFirst = new JButton("Choose First Image");
        JButton chooseSecond = new JButton("Choose Second Image");

        String[] operations = {"Addition", "Subtraction", "Multiplication", "Division"};
        JComboBox<String> operationList = new JComboBox<>(operations);

        chooseFirst.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int rval = chooser.showOpenDialog(this);
            if (rval == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    biba = ImageIO.read(file);
                    chooseFirst.setText(file.getName());
                    if (biba.getType() != BufferedImage.TYPE_INT_RGB) {
                        BufferedImage biba = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    }
                } catch (IOException ex) {
                    System.out.println("Image could not be read");
                    System.exit(1);
                }
            }
        });
        chooseSecond.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int rval = chooser.showOpenDialog(this);
            if (rval == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    biba2 = ImageIO.read(file);
                    chooseSecond.setText(file.getName());
                    if (biba2.getType() != BufferedImage.TYPE_INT_RGB) {
                        BufferedImage biba2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    }
                } catch (IOException ex) {
                    System.out.println("Image could not be read");
                    System.exit(1);
                }
            }
        });

        JButton applyOperation = new JButton("Apply Operation");
        applyOperation.addActionListener(e -> {
            // Get selected operation
            String operation = (String) operationList.getSelectedItem();
            switch (operation) {
                case "Addition":
                    finalbiba = Addition(biba, biba2);
                    break;
                case "Subtraction":
                    finalbiba = Subtraction(biba, biba2);
                    break;
                case "Multiplication":
                    finalbiba = Multiplication(biba, biba2);
                    break;
                case "Division":
                    finalbiba = Dividing(biba, biba2);
                    break;
            }
            // Close the arithmetic operation dialog
            operationDialog.dispose();

            // Open a new window to display the finalbiba image
            displayFinalImage(finalbiba);
        });

        operationDialog.add(chooseFirst);
        operationDialog.add(chooseSecond);
        operationDialog.add(new JLabel("Select Operation:"));
        operationDialog.add(operationList);
        operationDialog.add(applyOperation);
        operationDialog.setLocationRelativeTo(f); // Center dialog relative to the frame
    }


    private void displayFinalImage(BufferedImage image) {
        // Create a new frame to display the image
        JFrame imageFrame = new JFrame("Processed Image");
        imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        imageFrame.setLayout(new BorderLayout());
    
        // Convert BufferedImage to ImageIcon
        ImageIcon imageIcon = new ImageIcon(image);
        JLabel imageLabel = new JLabel(imageIcon);
        imageFrame.add(imageLabel, BorderLayout.CENTER);
    
        // Pack the frame to the size of the image
        imageFrame.pack();
        imageFrame.setLocationRelativeTo(null);
        imageFrame.setVisible(true);
    }

    public void initBitwiseOperationDialog(){
        BitwiseDialog = new JDialog((Frame) null, "Adjust Bitwise Operation", true); // Make it modal
        BitwiseDialog.setLayout(new FlowLayout());
        BitwiseDialog.setSize(800, 150); // Or adjust size as needed
        JButton chooseFirst = new JButton("Choose First Image");
        JButton chooseSecond = new JButton("Choose Second Image");

        String[] operations = {"AND", "OR", "XOR", "ROI"};
        JComboBox<String> operationList = new JComboBox<>(operations);

        chooseFirst.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int rval = chooser.showOpenDialog(this);
            if (rval == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    biba = ImageIO.read(file);
                    chooseFirst.setText(file.getName());
                    if (biba.getType() != BufferedImage.TYPE_INT_RGB) {
                        BufferedImage biba = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    }
                } catch (IOException ex) {
                    System.out.println("Image could not be read");
                    System.exit(1);
                }
            }
        });
        chooseSecond.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int rval = chooser.showOpenDialog(this);
            if (rval == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    biba2 = ImageIO.read(file);
                    chooseSecond.setText(file.getName());
                    if (biba2.getType() != BufferedImage.TYPE_INT_RGB) {
                        BufferedImage biba2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    }
                } catch (IOException ex) {
                    System.out.println("Image could not be read");
                    System.exit(1);
                }
            }
        });

        JButton applyOperation = new JButton("Apply Operation");
        applyOperation.addActionListener(e -> {
            // Get selected operation
            String operation = (String) operationList.getSelectedItem();
            switch (operation) {
                case "AND":
                    finalbiba = BitwiseAND(biba, biba2);
                    break;
                case "OR":
                    finalbiba = BitwiseOR(biba, biba2);
                    break;
                case "XOR":
                    finalbiba = BitwiseXOR(biba, biba2);
                    break;
                case "ROI":
                    finalbiba = applyROI(biba, biba2);
                    break;
            }
            // Close the arithmetic operation dialog
            BitwiseDialog.dispose();

            // Open a new window to display the finalbiba image
            displayFinalImage(finalbiba);
        });

        BitwiseDialog.add(chooseFirst);
        BitwiseDialog.add(chooseSecond);
        BitwiseDialog.add(new JLabel("Select Operation:"));
        BitwiseDialog.add(operationList);
        BitwiseDialog.add(applyOperation);
        BitwiseDialog.setLocationRelativeTo(f); // Center dialog relative to the frame
    }

    public void actionPerformed(ActionEvent e) {

        if(e.getSource() instanceof JButton){
            if(e.getActionCommand().equals("Undo")){
                biFiltered = undo1;
                bibFiltered = undo2;
                repaint();
                return;
            } else if (e.getActionCommand().equals("Change Top image")){
                JFileChooser chooser = new JFileChooser();
                int rval = chooser.showOpenDialog(this);
                if (rval == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try {
                        bi = ImageIO.read(file);
                        if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                            BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                            Graphics big = bi2.getGraphics();
                            big.drawImage(bi, 0, 0,512,512, null);
                            biFiltered = bi = bi2;  
                            repaint();
                        }
                    } catch (IOException ex) {
                    }
                }
            } else if(e.getActionCommand().equals("Change Bottom image")){
                JFileChooser chooser = new JFileChooser();
                int rval = chooser.showOpenDialog(this);
                if (rval == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    if (file.getName().endsWith(".raw")) {
                        try {
                            FileInputStream fis = new FileInputStream(file);
                            byte[] data = new byte[(int) file.length()];
                            fis.read(data);
                            fis.close();
                            int wr = (int) Math.sqrt(data.length);
                            int hr = wr;
                            bib = new BufferedImage(wr, hr, BufferedImage.TYPE_BYTE_GRAY);
                            int index = 0;
                            for (int i = 0; i < hr; i++) {
                                for (int j = 0; j < wr; j++) {
                                    int gray = data[index] & 0xff;
                                    index++;
                                    int p = (gray << 16) | (gray << 8) | gray;
                                    bib.setRGB(j, i, p);
                                }
                            }
                            // Scale the image to 512x512
                            Image scaledImage = bib.getScaledInstance(512, 512, Image.SCALE_DEFAULT);
                            bib = new BufferedImage(512, 512, BufferedImage.TYPE_BYTE_GRAY);
                            Graphics2D g2d = bib.createGraphics();
                            g2d.drawImage(scaledImage, 0, 0, null);
                            g2d.dispose();
                            bibFiltered = bib;
                            f.setSize(2 * w + 10, 2 * h + 10);
                            repaint();
                        } catch (IOException ex) {
                        }
                    }
                    else {
                    try {
                        bib = ImageIO.read(file);
                        if (bib.getType() != BufferedImage.TYPE_INT_RGB) {
                            BufferedImage bib2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                            Graphics big = bib2.getGraphics();
                            big.drawImage(bib, 0, 0, 512,512, null);
                            bibFiltered = bib = bib2;
                            f.setSize(2 * w + 10, 2 * h + 10);
                            repaint();
                        }
                    } catch (IOException ex) {
                    }
                }
            }
            } 
        }
        JComboBox cb = (JComboBox)e.getSource();
        if (cb.getActionCommand().equals("SetFilter")) {
            setOpIndex(cb.getSelectedIndex());
                if (opIndex == 2) {
                    updateSliderForOperation();
                    sliderDialog.setVisible(true);
                } else if (opIndex == 3) {
                    updateSliderForOperation();
                    sliderDialog.setVisible(true);
                } else if (opIndex == 5) {
                    operationDialog.setVisible(true);
                } else if (opIndex == 7) {
                    BitwiseDialog.setVisible(true);
                }  else if (opIndex == 9) {
                    updateSliderForOperation();
                    sliderDialog.setVisible(true);
                }   else if (opIndex == 11) {
                    updateSliderForOperation();
                    sliderDialog.setVisible(true);
                }   else if(opIndex == 13){
                    convolutionDialog.setVisible(true);
                }   else if(opIndex == 15){
                    FilterDialog.setVisible(true);
                } else if(opIndex == 17){
                    updateSliderForOperation();
                    sliderDialog.setVisible(true);
                }
                else{
                    sliderDialog.setVisible(false);
                    operationDialog.setVisible(false);
                    repaint();
                }
            }
        else if (cb.getActionCommand().equals("Formats")) {
            String format = (String)cb.getSelectedItem();
            File saveFile = new File("savedimage."+format);
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(saveFile);
            int rval = chooser.showSaveDialog(cb);
            if (rval == JFileChooser.APPROVE_OPTION) {
                saveFile = chooser.getSelectedFile();
                try {
                    ImageIO.write(biFiltered, format, saveFile);
                    ImageIO.write(bibFiltered, format, saveFile);
                } catch (IOException ex) {
                }
            }
        }  
        if (e.getSource() instanceof JSlider) {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                scalingFactor = (int)source.getValue();
            }
        }
        if (e.getSource() instanceof JCheckBox){
            toggle = !toggle;
        }
    };

    public static void main(String[] args) {
        
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        // Add a checkbox to toggle between applying filters consecutively or starting fresh from the original image
        String imagePath = args.length > 0 ? args[0] : null;
        Demo de = new Demo(imagePath);
        f.add("Center", de);
        JComboBox<String> choices = new JComboBox<>(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);
        JComboBox<String> formats = new JComboBox<>(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);
        JButton button = new JButton("Undo");
        button.setActionCommand("Undo");
        button.addActionListener(de);
        JButton addTop = new JButton("Change Top image");
        addTop.setActionCommand("Change Top image");
        addTop.addActionListener(de);
        JButton addBottom = new JButton("Change Bottom image");
        addBottom.setActionCommand("Change Bottom image");
        addBottom.addActionListener(de);
        JCheckBox applyConsecutivelyCheckbox = new JCheckBox("Apply Filters Consecutively", false);
        applyConsecutivelyCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e){
                toggle = !toggle;
            }
        });
        JPanel panel = new JPanel();
        panel.add(choices);
        panel.add(new JLabel("Save As"));
        panel.add(formats);
        panel.add(button);
        panel.add(addTop);
        panel.add(addBottom);   
        panel.add(applyConsecutivelyCheckbox);
        f.add("North", panel);
        f.pack();
        f.setVisible(true);
    }

}