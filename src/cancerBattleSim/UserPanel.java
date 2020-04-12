package cancerBattleSim;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.userpanel.ui.UserPanelCreator;

public class UserPanel implements UserPanelCreator {

	private JLabel label_ccells_count = new JLabel("Initial CCells:");
	private JLabel label_ncells_count = new JLabel("Initial NCells:");
	private JLabel label_nkcells_count = new JLabel("Initial NKells:");

	private JLabel ccells_count = new JLabel();
	private JLabel ncells_count = new JLabel();
	private JLabel nkcells_count = new JLabel();
    
    private JButton buttonDimensions = new JButton("Create space");
    
	@Override
	public JPanel createPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);
        
        Parameters params = RunEnvironment.getInstance().getParameters();
		
        // add components to the panel
        constraints.gridx = 0;
        constraints.gridy = 0;     
        panel.add(label_ccells_count, constraints);
 
        constraints.gridx = 1;
        int ccellCount = params.getInteger("ccell_count");
        ccells_count.setText(String.valueOf(ccellCount));
        panel.add(ccells_count, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 1;     
        panel.add(label_ncells_count, constraints);
 
        constraints.gridx = 1;
        int ncellCount = params.getInteger("ncell_count");
        ncells_count.setText(String.valueOf(ncellCount));
        panel.add(ncells_count, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 2;     
        panel.add(label_nkcells_count, constraints);
 
        constraints.gridx = 1;
        int nkcellCount = params.getInteger("nkcell_count");
        nkcells_count.setText(String.valueOf(nkcellCount));
        panel.add(nkcells_count, constraints);
        
        
		return panel;
	}

}
