package routing;

import core.*;

public class SnWDecisionEngineRouting implements RoutingDecisionEngine { // Deklarasi kelas SnWDecisionEngineRouting
                                                                         // yang mengimplementasikan
                                                                         // RoutingDecisionEngine interface

    /** Inisiasi Variable Final */
    public static final String numberOfCopies = "nrofCopies"; // Variabel final untuk jumlah salinan
    public static final String binaryMode = "binaryMode"; // Variabel final untuk mode biner
    public static final String SprayAndWait_nm = "SnWDecisionEngine"; // Nama ruang nama untuk Spray and Wait
    public static final String msg_count_property = SprayAndWait_nm + "." + "copies"; // Properti untuk jumlah pesan

    public int initalNumberOfCopies; // Jumlah awal salinan
    public boolean isBinary; // Apakah mode biner atau tidak

    /** Make a contructor */
    public SnWDecisionEngineRouting(Settings settings) { // Konstruktor untuk inisialisasi
        // make an object for setting
        Settings SnWDecisionEngineSet = new Settings(SprayAndWait_nm); // Membuat objek Settings dengan nama ruang nama
                                                                       // Spray and Wait

        // make the initial number of copies
        initalNumberOfCopies = SnWDecisionEngineSet.getInt(numberOfCopies); // Mengambil nilai jumlah salinan awal
        isBinary = SnWDecisionEngineSet.getBoolean(binaryMode); // Mengambil nilai mode biner
    }

    /**
     * Make the copy constructor
     * 
     * @param snwDecisionEngine
     */
    protected SnWDecisionEngineRouting(SnWDecisionEngineRouting snWDecisionEngine) { // Konstruktor salinan
        initalNumberOfCopies = snWDecisionEngine.initalNumberOfCopies; // Menginisialisasi jumlah salinan
        isBinary = snWDecisionEngine.isBinary; // Menginisialisasi mode biner
    }

    @Override
    public RoutingDecisionEngine replicate() { // Metode untuk membuat salinan objek
        return new SnWDecisionEngineRouting(this); // Mengembalikan salinan objek saat ini
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) { // Metode ketika koneksi mati
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) { // Metode ketika koneksi aktif
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) { // Melakukan pertukaran untuk koneksi baru
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost host) { // Menentukan apakah tujuan akhir pesan
        Integer nrofCopies = (Integer) m.getProperty(msg_count_property); // Mendapatkan jumlah salinan pesan
        nrofCopies = (int) Math.ceil(nrofCopies / 2.0); // Mengurangi jumlah salinan menjadi setengah
        m.updateProperty(msg_count_property, nrofCopies); // Memperbarui properti jumlah pesan

        return m.getTo() == host; // Mengembalikan apakah tujuan akhir adalah host saat ini
    }

    @Override
    public boolean newMessage(Message m) { // Menentukan apakah pesan baru
        m.addProperty(msg_count_property, initalNumberOfCopies); // Menambahkan properti jumlah pesan ke pesan
        return true; // Mengembalikan true karena pesan baru ditambahkan
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) { // Menentukan apakah pesan
                                                                                             // harus dikirim ke host
                                                                                             // lain
        if (m.getTo() == otherHost) // Jika tujuan pesan adalah host lain
            return true; // Mengembalikan true

        int numberOfCopies = (Integer) m.getProperty(msg_count_property); // Mendapatkan jumlah salinan pesan

        SnWDecisionEngineRouting de = this.getOtherSnWDecisionEngine(otherHost); // Mendapatkan SnWDecisionEngineRouting
                                                                                 // dari host lain

        // Lakukan pengecekan apakah pesan harus dikirim ke host lain
        return de != null && numberOfCopies > 1; // Mengembalikan true jika de tidak null dan jumlah salinan lebih dari
                                                 // 1
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost host) { // Menentukan apakah pesan lama harus dihapus
        return m.getTo() == host; // Mengembalikan true jika tujuan pesan adalah host saat ini
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) { // Menentukan apakah pesan yang diterima
                                                                            // harus disimpan
        return m.getTo() != thisHost; // Mengembalikan true jika tujuan pesan bukan host saat ini
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost host) { // Menentukan apakah pesan yang dikirim harus
                                                                      // dihapus
        int nrofCopies;

        nrofCopies = (Integer) m.getProperty(msg_count_property); // Mendapatkan jumlah salinan pesan

        if (nrofCopies > 1) // Jika jumlah salinan lebih dari 1
            nrofCopies /= 2; // Mengurangi jumlah salinan menjadi setengah
        else
            return true; // Jika jumlah salinan adalah 1, maka kembalikan true

        m.updateProperty(msg_count_property, nrofCopies); // Memperbarui properti jumlah pesan

        return false; // Mengembalikan false karena pesan tidak perlu dihapus
    }

    private SnWDecisionEngineRouting getOtherSnWDecisionEngine(DTNHost h) { // Mendapatkan SnWDecisionEngineRouting dari
                                                                            // host lain
        MessageRouter otherRouter = h.getRouter(); // Mendapatkan router dari host lain
        assert otherRouter instanceof DecisionEngineRouter : "This router only works " +
                " with other routers of same type"; // Memastikan bahwa router adalah DecisionEngineRouter

        DecisionEngineRouter decisionEngineRouter = (DecisionEngineRouter) otherRouter; // Cast router menjadi
                                                                                        // DecisionEngineRouter
        return (SnWDecisionEngineRouting) decisionEngineRouter.getDecisionEngine(); // Mengembalikan
                                                                                    // SnWDecisionEngineRouting
    }

    @Override
    public void update(DTNHost thisHost) {
    } // Metode untuk memperbarui host
}
