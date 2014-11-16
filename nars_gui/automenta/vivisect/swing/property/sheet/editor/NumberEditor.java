/*
 * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: José Pinto
 * Nov 16, 2012
 */
package automenta.vivisect.swing.property.sheet.editor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JTextField;
import javax.swing.UIManager;

import automenta.vivisect.swing.property.beans.editor.AbstractPropertyEditor;
import automenta.vivisect.swing.property.swing.LookAndFeelTweaks;

/**
 * @author zp
 * 
 */
public class NumberEditor extends AbstractPropertyEditor {

	private double minVal, maxVal;
	private Object lastGoodValue;
	protected NumberFormat format;
	    
	    
	public NumberEditor(double minVal, double maxVal, int fracDigits) {
	    editor = new JTextField();
        ((JTextField)editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
        this.minVal = minVal;
        this.maxVal = maxVal;
		
		if(fracDigits == 0) {
			format = NumberFormat.getIntegerInstance();			
		}
		else {
			format = new DecimalFormat("0.0########");
			((DecimalFormat)format).setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
		    format.setMaximumFractionDigits(fracDigits);			
		}
		format.setGroupingUsed(false);		
	}
	
   
    public Object getValue() {
        String text = ((JTextField) editor).getText();
        if (text == null || text.trim().length() == 0) {
            return getDefaultValue();
        }

        // collect all numbers from this textfield
        StringBuffer number = new StringBuffer();
        number.ensureCapacity(text.length());
        for (int i = 0, c = text.length(); i < c; i++) {
            char character = text.charAt(i);
            if ('.' == character || '-' == character || 'E' == character || Character.isDigit(character)) {
                number.append(character);
            }
            else if (' ' == character) {
                continue;
            }
            else {
                break;
            }
        }
        
        Object before = lastGoodValue;
        try {        	
        	lastGoodValue = Double.parseDouble(number.toString());
        	if ((double)lastGoodValue > maxVal)
        		throw new Exception(lastGoodValue + " is too large");
        	if ((double)lastGoodValue < minVal)
        		throw new Exception(lastGoodValue + " is too small");
        }
        catch (Exception e) {
        	lastGoodValue = before;
            UIManager.getLookAndFeel().provideErrorFeedback(editor);
        }

        return lastGoodValue;
    }

    public void setValue(Object value) {
        if (value instanceof Number) {
            ((JTextField) editor).setText(format.format(((Number)value).doubleValue()));
        }
        else {
            ((JTextField) editor).setText("" + getDefaultValue());
        }
        lastGoodValue = value;
    }

    private Object getDefaultValue() {
       return 0d;
    }
    
    public static class IntegerEditor extends NumberEditor {
    	public IntegerEditor() {
    		super(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
    	}
    }
    
    public static class ShortEditor extends NumberEditor {
    	public ShortEditor() {
    		super(Short.MIN_VALUE, Short.MAX_VALUE, 0);
    	}
    }
    
    public static class ByteEditor extends NumberEditor {
    	public ByteEditor() {
    		super(Byte.MIN_VALUE, Byte.MAX_VALUE, 0);
    	}
    }
    
    public static class LongEditor extends NumberEditor {
    	public LongEditor() {
    		super(Long.MIN_VALUE, Long.MAX_VALUE, 0);
    	}
    }
    
    public static class FloatEditor extends NumberEditor {
    	public FloatEditor() {
    		super(-Float.MAX_VALUE, Float.MAX_VALUE, 4);
    	}
    }
    
    public static class DoubleEditor extends NumberEditor {
    	public DoubleEditor() {
    		super(-Double.MAX_VALUE, Double.MAX_VALUE, 12);
    	}
    }
}
