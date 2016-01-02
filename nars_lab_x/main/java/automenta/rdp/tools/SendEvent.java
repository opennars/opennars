/* SendEvent.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 * 
 */
package automenta.rdp.tools;

import automenta.rdp.Input;
import automenta.rdp.Rdp;

public class SendEvent extends JFrame {

	private static final long serialVersionUID = 6809307310347210557L;

	private javax.swing.JPanel jContentPane = null;

	private JTextField inputTypeField = null;

	private JLabel jLabel = null;

	private JTextField flagsField = null;

	private JLabel jLabel1 = null;

	private JTextField param1Field = null;

	private JLabel jLabel2 = null;

	private JTextField param2Field = null;

	private JLabel jLabel3 = null;

	private JButton jButton = null;

	private JTextField flagMaskField = null;

	private JButton jButton1 = null;

	Rdp rdp;

	/**
	 * This is the default constructor
	 */
	public SendEvent(Rdp rdp) {
		super();
		initialize();
		this.rdp = rdp;
	}

	public SendEvent() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
		this.setTitle("Send Event");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel3 = new JLabel();
			jLabel2 = new JLabel();
			jLabel1 = new JLabel();
			jLabel = new JLabel();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new GridBagLayout());
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 0;
			jLabel.setText("Input Type");
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 1;
			jLabel1.setText("Flags");
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 3;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 3;
			jLabel2.setText("Param 1");
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 5;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 5;
			jLabel3.setText("Param 2");
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.gridy = 6;
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.gridy = 2;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridx = 2;
			gridBagConstraints11.gridy = 2;
			jContentPane.add(getInputTypeField(), gridBagConstraints1);
			jContentPane.add(getParam1Field(), gridBagConstraints5);
			jContentPane.add(jLabel, gridBagConstraints2);
			jContentPane.add(getFlagsField(), gridBagConstraints3);
			jContentPane.add(jLabel1, gridBagConstraints4);
			jContentPane.add(jLabel2, gridBagConstraints6);
			jContentPane.add(getParam2Field(), gridBagConstraints7);
			jContentPane.add(jLabel3, gridBagConstraints8);
			jContentPane.add(getJButton(), gridBagConstraints9);
			jContentPane.add(getFlagMaskField(), gridBagConstraints10);
			jContentPane.add(getJButton1(), gridBagConstraints11);
		}
		return jContentPane;
	}

	/**
	 * This method initializes inputTypeField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getInputTypeField() {
		if (inputTypeField == null) {
			inputTypeField = new JTextField();
		}
		return inputTypeField;
	}

	/**
	 * This method initializes flagsField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getFlagsField() {
		if (flagsField == null) {
			flagsField = new JTextField();
		}
		return flagsField;
	}

	/**
	 * This method initializes param1Field
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getParam1Field() {
		if (param1Field == null) {
			param1Field = new JTextField();
		}
		return param1Field;
	}

	/**
	 * This method initializes param2Field
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getParam2Field() {
		if (param2Field == null) {
			param2Field = new JTextField();
		}
		return param2Field;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Send Event");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (rdp != null) {
						rdp.sendInput(Input.getTime(), Integer.decode(
								inputTypeField.getText()).intValue(), Integer
								.decode(flagsField.getText()).intValue(),
								Integer.decode(param1Field.getText())
										.intValue(), Integer.decode(
										param2Field.getText()).intValue());
					}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes flagMaskField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getFlagMaskField() {
		if (flagMaskField == null) {
			flagMaskField = new JTextField();
		}
		return flagMaskField;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Apply Mask");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// apply the mask to the flags field
					flagsField.setText("0x"
							+ Integer.toHexString(Integer.decode(
									flagsField.getText()).intValue()
									| Integer.decode(flagMaskField.getText())
											.intValue()));
					flagMaskField.setText("");
				}
			});
		}
		return jButton1;
	}
}
