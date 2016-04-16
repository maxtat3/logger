package domain;

/**
 * Channels and their samples per second.
 */
public class Channel {
    // Variables only for channel 1
    private final SpsItem ch1And60sps;
    private final SpsItem ch1And30sps;
    private final SpsItem ch1And5sps;
    // Variables only for channel 1 and 2
    private final SpsItem ch2And30sps;
    private final SpsItem ch2And10sps;
    private final SpsItem ch2And5sps;
    // Variables only for channel 1, 2 and 3
    private final SpsItem ch3And20sps;
    private final SpsItem ch3And5sps;
    // Variables only for channel 1, 2, 3 and 4
    private final SpsItem ch4And15sps;
    private final SpsItem ch4And5sps;


    public Channel() {
        ch1And60sps = new SpsItem(Channels.ONE, "a", "60 sps");
        ch1And30sps = new SpsItem(Channels.ONE, "b", "30 sps");
        ch1And5sps = new SpsItem(Channels.ONE, "c", "5 sps");

        ch2And30sps = new SpsItem(Channels.TWO, "d", "30 sps");
        ch2And10sps = new SpsItem(Channels.TWO, "e", "10 sps");
        ch2And5sps = new SpsItem(Channels.TWO, "f", "5 sps");

        ch3And20sps = new SpsItem(Channels.THREE, "g", "20 sps");
        ch3And5sps = new SpsItem(Channels.THREE, "h", "5 sps");

        ch4And15sps = new SpsItem(Channels.FOUR, "k", "15 sps");
        ch4And5sps = new SpsItem(Channels.FOUR, "l", "5 sps");
    }


    public SpsItem getCh1And60sps() {
        return ch1And60sps;
    }

    public SpsItem getCh1And30sps() {
        return ch1And30sps;
    }

    public SpsItem getCh1And5sps() {
        return ch1And5sps;
    }

    public SpsItem getCh2And30sps() {
        return ch2And30sps;
    }

    public SpsItem getCh2And10sps() {
        return ch2And10sps;
    }

    public SpsItem getCh2And5sps() {
        return ch2And5sps;
    }

    public SpsItem getCh3And20sps() {
        return ch3And20sps;
    }

    public SpsItem getCh3And5sps() {
        return ch3And5sps;
    }

    public SpsItem getCh4And15sps() {
        return ch4And15sps;
    }

    public SpsItem getCh4And5sps() {
        return ch4And5sps;
    }

    /**
     * Для каждой комбинации "количество каналов : частота выборок"
     * на стороне mcu есть своя задержка , соответственно и своя
     * команда {@link SpsItem#cmd} отправляемая mcu по USART .
     * Это означает - максимальное кол-во задествованых каналов *
     * коли-во выборок для каждого канала =
     * количество команд .
     * Например:
     * 1 канал задействовано * 4 варинта выборок
     * 2 канала задействовано * 3 варинта выборок
     * 3 канала задействовано * 3 варинта выборок
     * 4 канала задействовано * 2 варинта выборок
     * 4 + 3 + 3 + 2 = 12 команд.
     */
    public class SpsItem {

        /**
         * In measure process active channels.
         *
         * Номера каналов которые могу выбиратся только последовательно.
         * Например: 1, 2 или 1, 2, 3, 4  ...
         * В этом случае перечисление {@link Channels} указывает
         * номера последовательно задействованных каналов.
         * For example channels = 3 {@link Channels#THREE}
         * it means that active are 1, 2, 3 numbers channels.
         *
         * @see Channels
         */
        private Channels channelsConsistent = null;

        /**
         * Массив хранящий независимую последовательность
         * задействованных каналов.
         * Например {@link Channels#ONE} и {@link Channels#FOUR}
         * означает что задействованы только канал 1 и 4
         * остальные явяляются неактивными .
         *
         *  @see Channels
         */
        private Channels[] channelsNotConsistent = null;

        /**
         * Команда отсылаемая на устройство для каждого
         * варианта "кол-во задействованных каналов : частота выборок".
         * String for translated to device from USART.
         * for example: a , b , ...
         */
        private String cmd;
        /**
         * This is viewed text presentation in UI.
         * For example: "15 sps", "100 sps"
         */
        private String spsName;


        public SpsItem(Channels channelsConsistent, String cmd, String spsName) {
            this.channelsConsistent = channelsConsistent;
            this.cmd = cmd;
            this.spsName = spsName;
        }

        public SpsItem(String cmd, String spsName, Channels... channelsNotConsistent) {
            this.cmd = cmd;
            this.spsName = spsName;
            this.channelsNotConsistent = channelsNotConsistent;
        }


        public Channels getChannelsConsistent() {
            return channelsConsistent;
        }

        public Channels[] getChannelsNotConsistent() {
            return channelsNotConsistent;
        }

        public String getCmd() {
            return cmd;
        }

        public String getSpsName() {
            return spsName;
        }
    }

    /**
     * Numbers of channelsConsistent used in measure process.
     * If choose {@link Channels#THREE} value
     * Количество может трактоватся по разному и зависит от
     * реализации логики контроллера. Есть два возможных
     * варианта:
     * 1. каналы задействованы последовательно
     * {@link SpsItem#channelsConsistent}
     * 2. каналы задействованы независимо
     * {@link SpsItem#channelsNotConsistent}
     */
    public enum Channels {
        ONE(1, "1 Канал"),
        TWO(2, "2 Канала"),
        THREE(3, "3 Канала"),
        FOUR(4, "4 Канала");

        private int number;
        private String name;

        Channels(int number, String name) {
            this.number = number;
            this.name = name;
        }

        /**
         * Return in measure process active channel(-s) number presentation.
         * For example if channels = 3 {@link Channels#THREE}
         * it means that active are 1, 2, 3 numbers of channels.
         *
         * @return numbers of used channels
         */
        public int getNumber() {
            return number;
        }

        /**
         * Return in measure process active channel(-s) name.
         *
         * @return active channel(-s) name
         */
        public String getName() {
            return name;
        }
    }
}
