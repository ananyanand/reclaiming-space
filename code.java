import java.io.*;
import java.util.Scanner;

class Record {
    private int id;
    private String name;
    private int age;

    public Record(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Age: " + age;
    }
}

class RecordManager {
    private RandomAccessFile file;
    private RandomAccessFile deletedFile;

    public RecordManager(String filename) {
        try {
            file = new RandomAccessFile(filename, "rw");
            deletedFile = new RandomAccessFile("deleted_records.txt", "rw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void packRecord(Record record) {
        try {
            if (deletedFile.length() > 0) {
                long deletedRecordPosition = deletedFile.readLong();
                int deletedRecordSize = deletedFile.readInt();
                if (deletedRecordSize == getRecordSize()) {
                    file.seek(deletedRecordPosition);
                    file.writeInt(record.getId());
                    file.writeUTF(record.getName());
                    file.writeInt(record.getAge());
                    System.out.println("Record packed at position: " + deletedRecordPosition);
                    return;
                }
            }
            file.seek(file.length());
            file.writeInt(record.getId());
            file.writeUTF(record.getName());
            file.writeInt(record.getAge());
            System.out.println("Record packed at end of file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Record unpackRecord() {
        try {
            if (file.getFilePointer() >= file.length()) {
                return null; // No more records to unpack
            }
            int id = file.readInt();
            String name = file.readUTF();
            int age = file.readInt();
            return new Record(id, name, age);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void searchRecord(int id) {
        try {
            file.seek(0);
            while (file.getFilePointer() < file.length()) {
                long currentPosition = file.getFilePointer();
                int currentId = file.readInt();
                String name = file.readUTF();
                int age = file.readInt();
                if (currentId == id) {
                    System.out.println("Record found at position: " + currentPosition);
                    System.out.println("Record: " + new Record(currentId, name, age));
                    return;
                }
            }
            System.out.println("Record not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void modifyRecord(int id, String newName, int newAge) {
        try {
            file.seek(0);
            while (file.getFilePointer() < file.length()) {
                long currentPosition = file.getFilePointer();
                int currentId = file.readInt();
                if (currentId == id) {
                    file.writeUTF(newName);
                    file.writeInt(newAge);
                    System.out.println("Record modified at position: " + currentPosition);
                    return;
                } else {
                    file.readUTF();
                    file.readInt();
                }
            }
            System.out.println("Record not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getRecordSize() {
        return Integer.BYTES + Character.BYTES + (Character.SIZE / Byte.SIZE) * 100 + Integer.BYTES; // Size of ID + Size of name + Size of age
    }

    public void deleteRecord(int id) {
        try {
            file.seek(0);
            while (file.getFilePointer() < file.length()) {
                long currentPosition = file.getFilePointer();
                int currentId = file.readInt();
                if (currentId == id) {
                    int recordSize = getRecordSize();
                    file.seek(currentPosition);
                    file.writeInt(-1); // Mark the record as deleted with ID -1
                    file.skipBytes(recordSize - Integer.BYTES); // Skip name and age
                    deletedFile.seek(deletedFile.length());
                    deletedFile.writeLong(currentPosition);
                    deletedFile.writeInt(recordSize);
                    System.out.println("Record deleted at position: " + currentPosition);
                    return;
                } else {
                    file.readUTF();
                    file.readInt();
                }
            }
            System.out.println("Record not found.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            file.close();
            deletedFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class RecordManagementApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RecordManager recordManager = new RecordManager("records.txt");

        while (true) {
            System.out.println("1. Pack record");
            System.out.println("2. Unpack record");
            System.out.println("3. Search record");
            System.out.println("4. Modify record");
            System.out.println("5. Delete record");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter ID: ");
                    int id = scanner.nextInt();
                    System.out.print("Enter name: ");
                    String name = scanner.next();
                    System.out.print("Enter age: ");
                    int age = scanner.nextInt();
                    recordManager.packRecord(new Record(id, name, age));
                    break;
                case 2:
                    Record record = recordManager.unpackRecord();
                    if (record != null) {
                        System.out.println("Unpacked record: " + record);
                    } else {
                        System.out.println("No more records.");
                    }
                    break;
                case 3:
                    System.out.print("Enter ID to search: ");
                    int searchId = scanner.nextInt();
                    recordManager.searchRecord(searchId);
                    break;
                case 4:
                    System.out.print("Enter ID to modify: ");
                    int modifyId = scanner.nextInt();
                    System.out.print("Enter new name: ");
                    String newName = scanner.next();
                    System.out.print("Enter new age: ");
                    int newAge = scanner.nextInt();
                    recordManager.modifyRecord(modifyId, newName, newAge);
                    break;
                case 5:
                    System.out.print("Enter ID to delete: ");
                    int deleteId = scanner.nextInt();
                    recordManager.deleteRecord(deleteId);
                    break;
                case 6:
                    recordManager.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                    break;
            }
        }
    }
}
