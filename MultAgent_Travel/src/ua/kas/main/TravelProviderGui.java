package ua.kas.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

class TravelProviderGui extends JFrame {

	private static final long serialVersionUID = 1L;

	private TravelProvider myAgent;

	private JTextField titleField, sdField, priceField;

	TravelProviderGui(TravelProvider a) {
		super(a.getLocalName());

		myAgent = a;

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(3, 2));
		p.add(new JLabel("Destination city:"));
		titleField = new JTextField(15);
		p.add(titleField);

		p.add(new JLabel("Schedule departs:"));
		sdField = new JTextField(15);
		p.add(sdField);

		p.add(new JLabel("Price:"));
		priceField = new JTextField(15);
		p.add(priceField);
		getContentPane().add(p, BorderLayout.CENTER);

		JButton addButton = new JButton("ADD");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = titleField.getText().trim();
					String scheduleDeparting = sdField.getText().trim();
					String price = priceField.getText().trim();
					String[] dayMass = { "понедельник", "вторник", "среда", "четверг", "пятница", "cуббота",
							"воскресенье" };
					int day = 0;
					for (int i = 0; i < dayMass.length; i++) {
						if (scheduleDeparting.equals(dayMass[i])) {
							day = i;
						}
					}
					myAgent.updateCatalogue(title, price, day);
					titleField.setText("");
					priceField.setText("");
					sdField.setText("");
				} catch (Exception e) {
					JOptionPane.showMessageDialog(TravelProviderGui.this, "Invalid values. " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		});

		setResizable(false);
	}

	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int) screenSize.getWidth() / 2;
		int centerY = (int) screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}
}