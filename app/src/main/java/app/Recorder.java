package app;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provide save measure results to file system
 */
public class Recorder {

    public static final Logger LOG = Logger.getLogger(Recorder.class.getName());
    public static final String CSV_EXT_FILE = ".csv";
    public static final String PREFIX_FILE = "results_";

    public static final String LFCR = "\n";
    public static final String TAB = "\t";
    /**
     * This header in result file will be created.
     * This text is the first line.
     */
    private static final String HEADER_FILE = "NUMBER" + TAB + "CH1"+ TAB + "CH2"+ TAB + "CH3"+ TAB + "CH4";

    /**
     * This buffer contained channels values and other information to be recorded to result file.
     */
    private StringBuilder buffer = new StringBuilder();


    /**
     * Write measure results as text in file.
     * @param ch1 channel 1 results
     * @param allChannels how many channels have been involved in the measure process
     */
    public void writeResultsToFile(int allChannels, List<Integer> ch1, List<Integer> ch2,
                                   List<Integer> ch3, List<Integer> ch4) throws LargeChannelsRecordException {
        if (allChannels > 4) throw new LargeChannelsRecordException();
        try {
            FileWriter file = new FileWriter("./" + PREFIX_FILE + getCurrentDate() + CSV_EXT_FILE);
            buffer.append(HEADER_FILE).append(LFCR);
            int line = 0; // line number on which data was stored
            while ( (ch1.size() - 1) != line ){
                switch (allChannels) {
                    case 1:
                        buffer.append(line).append(TAB)
                                .append(ch1.get(line)).append(TAB)
                                .append(LFCR);
                        break;
                    case 2:
                        buffer.append(line).append(TAB)
                                .append(ch1.get(line)).append(TAB)
                                .append(ch2.get(line)).append(TAB)
                                .append(LFCR);
                        break;
                    case 3:
                        buffer.append(line).append(TAB)
                                .append(ch1.get(line)).append(TAB)
                                .append(ch2.get(line)).append(TAB)
                                .append(ch3.get(line)).append(TAB)
                                .append(LFCR);
                        break;
                    case 4:
                        buffer.append(line).append(TAB)
                                .append(ch1.get(line)).append(TAB)
                                .append(ch2.get(line)).append(TAB)
                                .append(ch3.get(line)).append(TAB)
                                .append(ch4.get(line)).append(TAB)
                                .append(LFCR);
                        break;
                }
                line++;
            }
            ch1.removeAll(ch1); //todo - may be replace to clear method, because this faster !
//            ch2.removeAll(ch2); // may be used this ?
//            ch3.removeAll(ch3);
//            ch4.removeAll(ch4);

            file.write(String.valueOf(buffer));
            file.flush(); //clear buffer and write to file
            file.close();
        } catch (IOException ex) {
            System.out.println("ошибка в методе writeResultsToFile !");
            LOG.log(Level.WARNING, "IO error !");
        }
    }

    /**
     * Get date when file will be created
     * @return string date presentation
     */
    private String getCurrentDate(){
        Date date = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd_MM_YYYY__HH_mm_ss");
        return formatDate.format(date);
    }

}
