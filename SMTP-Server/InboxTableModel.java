import javax.swing.table.AbstractTableModel;
import java.util.List;

public class InboxTableModel extends AbstractTableModel {
    private final String[] columnNames = {"From", "To", "Subject", "Date", "Message"};
    private List<Email> emails;

    public InboxTableModel(List<Email> emails) {
        this.emails = emails;
    }

    @Override
    public int getRowCount() {
        return emails.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Email email = emails.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return email.getFrom();
            case 1:
                return email.getTo();
            case 2:
                return email.getSubject();
            case 3:
                return email.getDate();
            case 4:
                return email.getMessage();
            default:
                return null;
        }
    }
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
}
