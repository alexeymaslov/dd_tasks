import java.util.*;
import java.io.*;

public class ExternalSort {

    public static void main(String[] args) throws IOException {
        if (args.length >= 2) {
            String inputFileName = args[0];
            String outputFileName = args[1];
            Comparator<String> comparator = new Comparator<String>() {
                public int compare(String str1, String str2) {
                    return str1.compareTo(str2);
                }
            };
            List<File> sortedFiles = produceSortedFilesFromBigFile(new File(inputFileName), comparator);
            mergeSortedFiles(sortedFiles, new File(outputFileName), comparator);

        } else {
            System.out.println("No input or output file names");
        }
    }

    private static List<File> produceSortedFilesFromBigFile (File file, Comparator<String> cmp) throws IOException {
        List<File> files = new ArrayList<File>();

        // На машине имеется 512 мб. Пусть джава занимает половину, четверть оставим под вспомогательное место для сортировки.
        // Тогда пусть будет 128 мб на файл.
        long fileSize = 128 * 1024 * 1024;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            List<String> content = new ArrayList<String>();
            String currentLine = "";

            try {
                while (currentLine != null) {
                    long currentSize = 0;
                    while (currentSize < fileSize 
                        && (currentLine = reader.readLine()) != null) {
                        content.add(currentLine);
                    	// один символ = 2 байта, плюс байты на оборачивание в объект ~ 40
                        currentSize += (currentLine.length() * 2 + 40);
                    }

                    files.add(produceSortedFile(content, cmp));
                    content.clear();
                }

            } catch (EOFException eof) {
                if (content.size() > 0) {
                    files.add(produceSortedFile(content, cmp));
                    content.clear();
                }
            }

        } finally {
            reader.close();
        }

        return files;
    }

    private static File produceSortedFile(List<String> content, Comparator<String> cmp) throws IOException  {
        Collections.sort(content,cmp);

        File newTempFile = File.createTempFile("sortedPart", null);
        newTempFile.deleteOnExit();

        BufferedWriter writer = new BufferedWriter(new FileWriter(newTempFile));
        try {
            for(String str : content) {
                writer.write(str);
                writer.newLine();
            }

        } finally {
            writer.close();
        }

        return newTempFile;
    }

    private static void mergeSortedFiles(List<File> files, File outputFile, Comparator<String> cmp) throws IOException {
    	// В очереди лежат файлы упорядоченные по первой строчке
        PriorityQueue<BinaryFileBuffer> priorityQueue = new PriorityQueue<BinaryFileBuffer>(11, 
            new Comparator<BinaryFileBuffer>() {
              public int compare(BinaryFileBuffer buf1, BinaryFileBuffer buf2) {
                return cmp.compare(buf1.peek(), buf2.peek());
              }
            }
        );

        for (File file : files) {
            BinaryFileBuffer buffer = new BinaryFileBuffer(file);
            priorityQueue.add(buffer);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            while(priorityQueue.size() > 0) {
                BinaryFileBuffer buffer = priorityQueue.poll();
                String str = buffer.pop();
                writer.write(str);
                writer.newLine();

                if(buffer.empty()) {
                    buffer.close();

                } else {
                    priorityQueue.add(buffer);
                }
            }

        } finally { 
            writer.close();
            for(BinaryFileBuffer buffer : priorityQueue) 
                buffer.close();
        }
    }


    // Обертка над файлом, чтобы работать с ним подобно стеку.
    static private class BinaryFileBuffer {

        private BufferedReader reader;
        private File file;
        private String cache;
        private boolean empty;
         
        public BinaryFileBuffer(File t_file) throws IOException {
            file = t_file;
            reader = new BufferedReader(new FileReader(file));
            reload();
        }
         
        public boolean empty() {
            return empty;
        }
        
        public void close() throws IOException {
            reader.close();
        }
         
        public String peek() {
            if(empty()) 
                return null;

            return cache.toString();
        }
        public String pop() throws IOException {
          String answer = peek();
            reload();
          return answer;
        }

        private void reload() throws IOException {
            try {
                if ((this.cache = reader.readLine()) == null) {
                    empty = true;
                    cache = null;

                } else {
                    empty = false;
                }

            } catch (EOFException oef) {
                empty = true;
                cache = null;
            }
        }
    }
}