package nars.util.utf8;

/**
 * Created by me on 5/4/15.
 */
/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public enum Byt {
    ;


    public static byte[] grow( byte[] array, int size ) {

        byte[] newArray = new byte[ array.length + size ];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }


    public static byte[] grow( byte[] array ) {
        return grow( array, array.length );
    }


    public static byte[] shrink( byte[] array, int size ) {

        int al = array.length - size;
        byte[] newArray = new byte[ al ];

        System.arraycopy( array, 0, newArray, 0, al );
        return newArray;
    }


    public static byte[] compact( byte[] array ) {

        int nullCount = 0;
        for ( byte ch : array ) {

            if ( ch == 0 ) {
                nullCount++;
            }
        }

        if (nullCount == 0) return array;

        byte[] newArray = new byte[ array.length - nullCount ];

        int j = 0;
        for ( byte ch : array ) {

            if ( ch == 0 ) {
                continue;
            }


            newArray[ j++ ] = ch;
        }
        return newArray;
    }


    /**
     * Creates an array of bytes
     *
     * @param size size of the array you want to make
     * @return array of bytes
     */
    public static byte[] arrayOfByte( int size ) {
        return new byte[ size ];
    }

//    /**
//     * @param array array
//     * @return array
//     */
//
//    public static byte[] array( final byte... array ) {
//        return array;
//    }
//
//    /**
//     * @param array array
//     * @return array
//     */
//
//    public static byte[] bytes( final byte... array ) {
//        return array;
//    }


    public static int len(byte[] array ) {
        return array.length;
    }



    public static int lengthOf(byte[] array ) {
        return array.length;
    }


    public static byte atIndex( byte[] array, int index ) {
        return idx(array, index);
    }


    public static byte idx( byte[] array, int index ) {
        int i = i(array, index);

        return array[ i ];
    }



    public static void atIndex( byte[] array, int index, byte value ) {
        idx(array, index, value);
    }



    public static void idx( byte[] array, int index, byte value ) {
        int i = i(array, index);

        array[ i ] = value;
    }



    public static byte[] sliceOf( byte[] array, int startIndex, int endIndex ) {
        return slc (array, startIndex, endIndex);
    }


    public static byte[] slc( byte[] array, int startIndex, int endIndex ) {

        int al = array.length;
        int start = i(al, startIndex);
        int end = e(al, endIndex);
        int newLength = end - start;

        /** if the actual length will be the same as the input array, it
         * means the entire string will be necessary and we are just going
         * to return the internal array and not make a copy of it.
         * this case is ideal
         */
        if (newLength == al)
            return array;

        else if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, al )
            );
        }
        else {
            return Arrays.copyOf(array, newLength);
        }
    }



    public static byte[] sliceOf( byte[] array, int startIndex ) {
        return slc(array, startIndex);
    }


    public static byte[] slc( byte[] array, int startIndex ) {

        int al = array.length;

        int start = i(al, startIndex);
        int newLength = al - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, al)
            );
        }

        byte[] newArray = new byte[ newLength ];
        System.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }



    public static byte[] endSliceOf( byte[] array, int endIndex ) {
        return slcEnd(array, endIndex);
    }


    public static byte[] slcEnd( byte[] array, int endIndex ) {

        int end = e(array, endIndex);
        int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        byte[] newArray = new byte[ newLength ];
        System.arraycopy( array, 0, newArray, 0, newLength );
        return newArray;
    }


    public static boolean in( int value, byte... array ) {
        for ( int currentValue : array ) {
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }



    public static boolean inIntArray( byte value, int[] array ) {
        for ( int currentValue : array ) {
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }



    public static boolean in( int value, int offset, byte[] array ) {
        for ( int index = offset; index < array.length; index++ ) {
            if (array[ index ] == value)
                return true;
        }
        return false;
    }


    public static boolean in( int value, int offset, int end, byte[] array ) {
        for ( int index = offset; index < end; index++ ) {
            if ( array[ index ] == value ) {
                return true;
            }
        }
        return false;
    }



    public static byte[] copy( byte[] array ) {
        return Arrays.copyOf(array, array.length);
//        //Exceptions.requireNonNull(array);
//        byte[] newArray = new byte[ array.length ];
//        System.arraycopy( array, 0, newArray, 0, array.length );
//        return newArray;
    }


    public static byte[] copy( byte[] array, int offset, int length ) {
        return Arrays.copyOfRange(array, offset, length);
//        //Exceptions.requireNonNull( array );
//        byte[] newArray = new byte[ length ];
//        System.arraycopy( array, offset, newArray, 0, length );
//        return newArray;
    }



    public static byte[] add( byte[] array, byte v ) {
        int al = array.length;
        byte[] newArray = new byte[ al + 1 ];
        System.arraycopy( array, 0, newArray, 0, al);
        newArray[al] = v;
        return newArray;
    }


    public static byte[] add( byte[] a, byte[] b ) {
        int al = a.length;
        int bl = b.length;
        byte[] newArray = new byte[ al + bl];
        System.arraycopy( a, 0, newArray, 0, al);
        System.arraycopy( b, 0, newArray, al, bl);
        return newArray;
    }



    public static byte[] insert( byte[] array, int idx, byte v ) {

        if ( idx >= array.length ) {
            return add( array, v );
        }

        int index = i(array, idx);

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        byte[] newArray = new byte[ array.length + 1 ];

        if ( index != 0 ) {
            /* Copy up to the length in the array before the index. */
            /*                 src     sbegin  dst       dbegin   length of copy */
            System.arraycopy( array, 0, newArray, 0, index );
        }


        boolean lastIndex = index == array.length - 1;
        int remainingIndex = array.length - index;

        System.arraycopy( array, index, newArray, index + 1, remainingIndex );

        newArray[ index ] = v;
        return newArray;
    }



    public static byte[] insert( byte[] array, int fromIndex, byte[] values ) {

        if ( fromIndex >= array.length ) {
            return add( array, values );
        }

        int index = i(array, fromIndex);

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        byte[] newArray = new byte[ array.length + values.length ];

        if ( index != 0 ) {
            /* Copy up to the length in the array before the index. */
            /*                 src     sbegin  dst       dbegin   length of copy */
            System.arraycopy( array, 0, newArray, 0, index );
        }


        boolean lastIndex = index == array.length - 1;

        int toIndex = index + values.length;
        int remainingIndex = newArray.length - toIndex;

        System.arraycopy( array, index, newArray, index + values.length, remainingIndex );

        for ( int i = index, j = 0; i < toIndex; i++, j++ ) {
            newArray[ i ] = values[ j ];
        }
        return newArray;
    }


    /* normalizes an index if it is negative, or greater than the length */
    private static int i(byte[] array, int index) {
        return i(array.length, index);
    }

    private static int i(int length, int index) {

        /* Adjust for reading from the right as in
        -1 reads the 4th element if the length is 5
         */
        if ( index < 0 ) {
            /* Bounds check
                if it is still less than 0, then they
                have an negative index that is greater than length
             */
            if ((index = length + index) < 0)
                return 0;
        }
        else if ( index >= length ) {
            return length - 1;
        }
        return index;
    }


    /* calculates ending index */
    private static int e(byte[] array, int index) {
        return e(array.length, index);
    }

    /* calculates ending index */
    private static int e(int length, int index) {


        /* Adjust for reading from the right as in
        -1 reads the 4th element if the length is 5
         */
        if ( index < 0 ) {
            /* Bounds check
                if it is still less than 0, then they
                have an negative index that is greater than length
             */
            if ((index = length + index) < 0)
                return 0;
        }
        else if ( index >length ) {
            return length;
        }
        return index;
    }

    public static int idxInt( byte[] bytes, int off ) {
        return ( ( bytes[ off + 3 ] & 0xFF ) ) +
                ( ( bytes[ off + 2 ] & 0xFF ) << 8 ) +
                ( ( bytes[ off + 1 ] & 0xFF ) << 16 ) +
                ( ( bytes[ off ] ) << 24 );
    }


    public static byte[] addInt( byte[] array, int v ) {

        byte[] arrayToHoldInt = new byte[ 4 ];
        intTo( arrayToHoldInt, 0, v );
        return add( array, arrayToHoldInt );

    }

    public static byte[] insertIntInto( byte[] array, int index, int v ) {

        byte[] arrayToHoldInt = new byte[ 4 ];
        intTo( arrayToHoldInt, 0, v );
        return insert( array, index, arrayToHoldInt );

    }


    public static void intTo( byte[] b, int off, int val ) {
        b[ off + 3 ] = ( byte ) ( val );
        b[ off + 2 ] = ( byte ) ( val >>> 8 );
        b[ off + 1 ] = ( byte ) ( val >>> 16 );
        b[ off ] = ( byte ) ( val >>> 24 );
    }

    public static void longTo( byte[] b, int off, long val ) {
        b[ off + 7 ] = ( byte ) ( val );
        b[ off + 6 ] = ( byte ) ( val >>> 8 );
        b[ off + 5 ] = ( byte ) ( val >>> 16 );
        b[ off + 4 ] = ( byte ) ( val >>> 24 );
        b[ off + 3 ] = ( byte ) ( val >>> 32 );
        b[ off + 2 ] = ( byte ) ( val >>> 40 );
        b[ off + 1 ] = ( byte ) ( val >>> 48 );
        b[ off ] = ( byte ) ( val >>> 56 );
    }

    public static byte[] addLong( byte[] array, long value ) {

        byte[] holder = new byte[ 8 ];
        longTo( holder, 0, value );
        return add( array, holder );

    }


    public static long idxUnsignedInt( byte[] bytes, int off ) {
        return ( ( bytes[ off + 3 ] & 0xFFL ) ) +
                ( ( bytes[ off + 2 ] & 0xFFL ) << 8L ) +
                ( ( bytes[ off + 1 ] & 0xFFL ) << 16L ) +
                ( ( bytes[ off ] & 0xFFL ) << 24L );
    }

    public static long idxLong( byte[] b, int off ) {
        return ( ( b[ off + 7 ] & 0xFFL ) ) +
                ( ( b[ off + 6 ] & 0xFFL ) << 8 ) +
                ( ( b[ off + 5 ] & 0xFFL ) << 16 ) +
                ( ( b[ off + 4 ] & 0xFFL ) << 24 ) +
                ( ( b[ off + 3 ] & 0xFFL ) << 32 ) +
                ( ( b[ off + 2 ] & 0xFFL ) << 40 ) +
                ( ( b[ off + 1 ] & 0xFFL ) << 48 ) +
                ( ( ( long ) b[ off ] ) << 56 );
    }


    public static short idxShort( byte[] b, int off ) {
        return ( short ) ( ( b[ off + 1 ] & 0xFF ) +
                ( b[ off ] << 8 ) );
    }

    public static byte[] addShort( byte[] array, short value ) {

        byte[] holder = new byte[ 2 ];
        shortTo( holder, 0, value );
        return add( array, holder );

    }


    public static byte[] insertShortInto( byte[] array, int index, short value ) {

        byte[] holder = new byte[ 2 ];
        shortTo( holder, 0, value );
        return insert( array, index, holder );

    }


    public static void shortTo( byte[] b, int off, short val ) {
        b[ off + 1 ] = ( byte ) ( val );
        b[ off ] = ( byte ) ( val >>> 8 );
    }


    public static char idxChar( byte[] b, int off ) {
        return ( char )  (( b[ off++ ] << 8 )  + ( b[ off  ] & 0xFF ));
    }

    public static byte[] addChar( byte[] array, char value ) {
        byte[] holder = new byte[ 2 ];
        charTo( holder, 0, value );
        return add( array, holder );
    }

    public static byte[] insertCharInto( byte[] array, int index, char value ) {
        byte[] holder = new byte[ 2 ];
        charTo( holder, 0, value );
        return insert( array, index, holder );

    }


    public static void charTo( byte[] b, int off, char val ) {
        b[ off++ ] = ( byte ) ( val >>> 8 );
        b[ off ] = ( byte ) ( val );
    }


    public static void charTo( byte[] b,  char val ) {
        b[ 1 ] = ( byte ) ( val );
        b[ 0 ] = ( byte ) ( val >>> 8 );
    }

    public static float idxFloat( byte[] array, int off ) {
        return Float.intBitsToFloat( idxInt( array, off ) );
    }

    public static byte[] addFloat( byte[] array, float value ) {

        byte[] holder = new byte[ 4 ];
        floatTo( holder, 0, value );
        return add( array, holder );

    }

    public static byte[] insertFloatInto( byte[] array, int index, float value ) {

        byte[] holder = new byte[ 4 ];
        floatTo( holder, 0, value );
        return insert( array, index, holder );

    }

    public static void floatTo( byte[] array, int off, float val ) {
        intTo( array, off, Float.floatToIntBits( val ) );
    }


    public static byte[] addDouble( byte[] array, double value ) {
        //Exceptions.requireNonNull( array );

        byte[] holder = new byte[ 4 ];
        doubleTo( holder, 0, value );
        return add( array, holder );

    }


    public static byte[] insertDoubleInto( byte[] array, int index, double value ) {
        //Exceptions.requireNonNull( array );

        byte[] holder = new byte[ 4 ];
        doubleTo( holder, 0, value );
        return insert( array, index, holder );

    }


    public static void doubleTo( byte[] b, int off, double val ) {
        longTo( b, off, Double.doubleToLongBits( val ) );
    }

    public static double idxDouble( byte[] b, int off ) {
        return Double.longBitsToDouble( idxLong( b, off ) );
    }

//
//
//    public static boolean booleanAt(byte[] b, int off) {
//        return b[off] != 0;
//    }
//
//
//    public static boolean booleanInBytePos1(int val) {
//        val = val & 0x01;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos2(int val) {
//        val = val & 0x02;
//        return val != 0;
//    }
//
//
//    public static boolean booleanInBytePos3(int val) {
//        val = val & 0x04;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos4(int val) {
//        val = val & 0x08;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos1(byte[] b, int off) {
//        int val = b[off] & 0x01;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos2(byte[] b, int off) {
//        int val = b[off] & 0x02;
//        return val != 0;
//    }
//
//
//    public static boolean booleanInBytePos3(byte[] b, int off) {
//        int val = b[off] & 0x04;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos4(byte[] b, int off) {
//        int val = b[off] & 0x08;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos5(byte[] b, int off) {
//        int val = b[off] & 0x10;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos6(byte[] b, int off) {
//        int val = b[off] & 0x20;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos7(byte[] b, int off) {
//        int val = b[off] & 0x40;
//        return val != 0;
//    }
//
//    public static boolean booleanInBytePos8(byte[] b, int off) {
//        int val = b[off] & 0x80;
//        return val != 0;
//    }
//
//
//    public static int byteAt(byte[] b, int off) {
//        return b[off];
//    }
//
//
//    public static int topNibbleAt(byte[] b, int off) {
//        return topNibbleAt (b[off] );
//    }
//
//    public static int bottomNibbleAt(byte[] b, int off) {
//        return bottomNibbleAt (b[off] );
//    }
//
//    public static int topNibbleAt(int val) {
//        return  (val & 0xF0);
//    }
//
//    public static int bottomNibbleAt(int val) {
//        return  (val & 0x0F);
//    }
//
//
//    public static char charAt1(byte[] b, int off) {
//        return (char) ((b[off + 1] & 0xFF) +
//                (b[off] << 8));
//    }
//


    public static int _idx( byte[] output, int outputOffset, byte[] input, int length ) {
        return _idx(output, outputOffset, input, 0, length);
    }


    public static int _idx( byte[] output, int outputOffset, byte[] input, int inputOffset, int length ) {
        System.arraycopy( input, inputOffset, output, outputOffset, length );
        return length;
    }


    public static int idxUnsignedShort( byte[] buffer, int off ) {

        int ch1 = buffer[ off++ ] & 0xFF;
        int ch2 = buffer[ off ] & 0xFF;

        return ( ch1 << 8 ) + ( ch2  );


    }

    public static short idxUnsignedByte( byte[] array, int location ) {
        return ( short ) ( array[ location ] & 0xFF );
    }


    public static String utfString( byte[] jsonBytes ) {
        return new String (jsonBytes, StandardCharsets.UTF_8);
    }



//    public static int reduceBy( final byte[] array, Object object ) {
//
//        int sum = 0;
//        for ( byte v : array ) {
//            sum = (int) Invoker.invokeReducer(object, sum, v);
//        }
//        return sum;
//    }


    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or throws exception if not.
     */
    public static boolean equalsOrDie(byte[] expected, byte[] got) {

        if (expected.length != got.length) {
            throw new RuntimeException("Lengths did not match, expected length " + expected.length +
                    " but got " + got.length);
        }

        for (int index=0; index< expected.length; index++) {
            if (expected[index]!= got[index]) {
                //TODO fix this
                throw new RuntimeException(Arrays.toString( new String[] { "value at index did not match index", String.valueOf(index), "expected value",
                        String.valueOf(expected[index]),
                        "but got", String.valueOf(got[index])} ));

            }
        }
        return true;
    }


    /**
     * Checks to see if two arrays are equals
     * @param expected expected array
     * @param got got array
     * @return true if equal or false if not.
     */
    public static boolean equals(byte[] expected, byte[] got) {

        if (expected.length != got.length) {
            return false;
        }

        for (int index=0; index< expected.length; index++) {
            if (expected[index]!= got[index]) {
                return false;
            }
        }
        return true;
    }

}
