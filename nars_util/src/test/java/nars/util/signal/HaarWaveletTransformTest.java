//package nars.util.signal;
//
//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Created by me on 10/26/15.
// */
//public class HaarWaveletTransformTest {
//
//    @Test
//    public void testHaarReversiblity() {
//        int[][] pixels = new int[4][4];
//        pixels[0][0] = 10;
//        pixels[0][1] = 5;
//        pixels[0][2] = 3;
//        pixels[0][3] = 2;
//
//        pixels[1][0] = 8;
//        pixels[1][1] = 4;
//        pixels[1][2] = 7;
//        pixels[1][3] = 9;
//
//        pixels[2][0] = 1;
//        pixels[2][1] = 5;
//        pixels[2][2] = 7;
//        pixels[2][3] = 6;
//
//        pixels[3][0] = 7;
//        pixels[3][1] = 5;
//        pixels[3][2] = 3;
//        pixels[3][3] = 1;
//
////        for (int i = 0; i < pixels.length; i++) {
////            for (int j = 0; j < pixels[0].length; j++) {
////                //System.out.print(pixels[i][j] + " ");
////            }
////            //System.out.println();
////        }
//        //System.out.println();
//        double[][] haar2DFWTransform = HaarWaveletTransform
//                .doHaar2DFWTransform(pixels, 1);
////        for (int i = 0; i < haar2DFWTransform.length; i++) {
////            for (int j = 0; j < haar2DFWTransform[0].length; j++) {
////                //System.out.print(haar2DFWTransform[i][j] + " ");
////            }
////            //System.out.println();
////        }
//        //System.out.println();
//
//
//        double[][] haar2DInvTransform = HaarWaveletTransform
//                .doHaar2DInvTransform(haar2DFWTransform, 1);
//
//        for (int i = 0; i < haar2DInvTransform.length; i++) {
//            for (int j = 0; j < haar2DInvTransform[0].length; j++) {
//
//                //equal to input:
//                assertEquals(pixels[i][j], haar2DInvTransform[i][j], 0.01);
//
//                //System.out.print(haar2DInvTransform[i][j] + " ");
//            }
//            //System.out.println();
//        }
//
//    }
// }