package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import JTreeManager.JTreeManager;
import searchfilters.TagTreeFilter;

/**
 * Classe implementant l'interface pour le filtre par tag
 *
 * @author Groupe PRO B-9
 */
public class TagFilter extends TreeFilter {

    /**
     *
     */
    private static final long serialVersionUID = -5442521176850560618L;
    private boolean tagChecked = false;

    /**
     * Constructeur
     *
     * @param manager jtree de la banque d'image
     */
    public TagFilter(final JTreeManager manager) {

        super(manager);

    }

    @Override
    protected void specialisation() {

        JCheckBox taggedCheckBox = new JCheckBox("tagged");
        taggedCheckBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/chbox-checked-20.png")));
        taggedCheckBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/chbox-unchecked-20.png")));
        
        taggedCheckBox.setForeground(GUIRender.getForeColor());
        taggedCheckBox.setBackground(GUIRender.getBackColor());
        
        taggedCheckBox.addItemListener((ItemEvent e) -> {
            tagChecked = e.getStateChange() == ItemEvent.SELECTED;
        });

        filter.addActionListener((ActionEvent e) -> {
            if (currentFilter != null) {
                manager.removeFiltre(currentFilter);
            }
            
            currentFilter = new TagTreeFilter(tagChecked);
            manager.addFiltre(currentFilter);
        });

        label = new JLabel("Tag");

        specialistationPanel.add(taggedCheckBox);

    }

}
