    import java.io.*;
    import java.util.TreeSet;
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
            "Random Look-Up Table"
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
        private JLabel sliderValueLabel; // Label to display slider value
        private int scalingFactor = 100; // Default scaling factor
        
        public Demo() {
            try {
                bi = ImageIO.read(new File("default.jpg"));

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

            // Image Negative Operation:
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

            // Image Negative Operation:
            for(int y=0; y<height; y++){
                for(int x =0; x<width; x++){
                    //int random = (int)(Math.random() * 255);
                    ImageArray[x][y][1] = Math.max(Math.min(Math.round(ImageArray[x][y][1]  + (int)(Math.random() * 511 - 255)), 255), 0);  //r
                    ImageArray[x][y][2] = Math.max(Math.min(Math.round(ImageArray[x][y][2]  + (int)(Math.random() * 511 - 255)), 255), 0);  //g
                    ImageArray[x][y][3] = Math.max(Math.min(Math.round(ImageArray[x][y][3]  + (int)(Math.random() * 511 - 255)), 255), 0);  //b
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

            // Image Negative Operation:
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

            // Image Negative Operation:
            for(int y=0; y<height; y++){
                for(int x =0; x<width; x++){
                    ImageArray[x][y][1] = Math.max(Math.min(Math.round(ImageArray[x][y][1]  * ImageArray2[x][y][1]/255), 255), 0);  //r
                    ImageArray[x][y][2] = Math.max(Math.min(Math.round(ImageArray[x][y][2]  * ImageArray2[x][y][2]/255), 255), 0);  //g
                    ImageArray[x][y][3] = Math.max(Math.min(Math.round(ImageArray[x][y][3]  * ImageArray2[x][y][3]/255), 255), 0);  //b
                }
            }
            
            return convertToBimage(ImageArray);  // Convert the array to BufferedImage
        }

        public BufferedImage Dividing(BufferedImage timg, BufferedImage timg2){
            int width = timg.getWidth();
            int height = timg.getHeight();

            int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array
            int[][][] ImageArray2 = convertToArray(timg2);          //  Convert the image to array

            // Image Negative Operation:
            for(int y=0; y<height; y++){
                for(int x =0; x<width; x++){
                    ImageArray[x][y][1] = Math.max(Math.min(Math.round(ImageArray[x][y][1]  / ImageArray2[x][y][1]*255), 255), 0);  //r
                    ImageArray[x][y][2] = Math.max(Math.min(Math.round(ImageArray[x][y][2]  / ImageArray2[x][y][2]*255), 255), 0);  //g
                    ImageArray[x][y][3] = Math.max(Math.min(Math.round(ImageArray[x][y][3]  / ImageArray2[x][y][3]*255), 255), 0);  //b
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
                    ImageArray[x][y][1] = ImageArray[x][y][1] | ImageArray2[x][y][1];  //r
                    ImageArray[x][y][2] = ImageArray[x][y][2] | ImageArray2[x][y][2];  //g
                    ImageArray[x][y][3] = ImageArray[x][y][3] | ImageArray2[x][y][3];  //b
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

            // Image Power-Low Function
            for(int y=0; y<height; y++){
                for(int x =0; x<width; x++){
                    ImageArray[x][y][1] = (int)(255 * Math.pow(ImageArray[x][y][1], scalingFactorFloat));  //r
                    ImageArray[x][y][2] = (int)(255 * Math.pow(ImageArray[x][y][2], scalingFactorFloat));  //g
                    ImageArray[x][y][3] = (int)(255 * Math.pow(ImageArray[x][y][3], scalingFactorFloat));  //b
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
        
        //************************************
        //  You need to register your functioin here
        //************************************
        public void filterImage() {
    
            if (opIndex == lastOp) {
                return;
            }

            uno_reverse_card = lastOp;
            lastOp = opIndex;

            switch (opIndex) {
            case 0: biFiltered = bi; /* original */
                    bibFiltered = bib;
                    return; 
            case 1: biFiltered = ImageNegative(bi); /* Image Negative */
                    if(bib != null){
                        bibFiltered = ImageNegative(bib);
                    }
                    return;
            case 2: 
                    biFiltered = ReScaleImage(bi, scalingFactor); /* Image Rescaling */
                    if(bib != null){
                        bibFiltered = ReScaleImage(bib, scalingFactor);
                    }
                    return;
            case 3: 
                    if(bib != null){
                        bibFiltered = Shift(bib, scalingFactor);
                    }
                    return;
            case 4:
                    biFiltered = RandomShift(bi);
                    if(bib != null){
                        bibFiltered = RandomShift(bib);
                    }
                    return;
            case 6:
                    biFiltered = BitwiseNOT(bi);
                    if(bib != null){
                        bibFiltered = BitwiseNOT(bib);
                    }
                    return;
            case 8:
                    biFiltered = LogFunction(bi);
                    if(bib != null){
                        bibFiltered = LogFunction(bib);
                    }
                    return;
            case 9:
                    biFiltered = PowerLow(bi, scalingFactor);
                    if(bib != null){
                        bibFiltered = PowerLow(bib, scalingFactor);
                    }
                    return;
            case 10:
                    biFiltered = RandomLookUpTable(bi);
                    if(bib != null){
                        bibFiltered = RandomLookUpTable(bib);
                    }
                    return;
            }
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
                        chooseFirst.setText(file.getName());
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
                    setOpIndex(uno_reverse_card);
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
        };
    
        public static void main(String s[]) {
            
            f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            });
            Demo de = new Demo();
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
            JPanel panel = new JPanel();
            panel.add(choices);
            panel.add(new JLabel("Save As"));
            panel.add(formats);
            panel.add(button);
            panel.add(addTop);
            panel.add(addBottom);   
            f.add("North", panel);
            f.pack();
            f.setVisible(true);
        }

    }