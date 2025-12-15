import java.util.*; // Java'nın koleksiyon sınıflarını (List, Map, Set vb.) kullanmak için import edilir.
// ====================================================================
// 1. Eşya Sınıfları
// ====================================================================
// Tüm eşyalar için soyut temel sınıf. Farklı türde eşyalar (anahtar, iksir, silah) bundan türetilir.
abstract class Item {
    protected String id; // Eşyanın benzersiz kimliği (sistem içi kullanım için).
    protected String name; // Eşyanın adı (envanter ve komutlarda oyuncuya gösterilir ve kullanılır).
    protected String description; // Eşyanın açıklaması, oyuncuya bilgi verir.
    // Constructor (Yapıcı Metot) ile eşyanın temel özelliklerini atama.
    public Item(String id , String name , String description ) {
        this.id = id ; // Kimliği atar.
        this.name = name ; // Adını atar.
        this.description = description ; // Açıklamasını atar.
    }
    // Eşya kullanıldığında çalışacak soyut metot. Her alt sınıfta farklı işlev göreceği için 'abstract' tanımlanır.
    public abstract void onUse(Player p , GameEngine ctx );
    // Eşyanın adını döndürür (getter metodu).
    public String getName() { return name; }
}
// ====================================================================
// Anahtar Eşyası
// ====================================================================
// Belirli bir yöndeki kilitli kapıyı açmak için kullanılan eşya sınıfı.
class KeyItem extends Item {
    private String unlockDirection; // Açacağı yönü (ör. "doğu") saklar.
    // Constructor, temel özellikleri ve anahtarın hangi yönü açacağını belirler.
    public KeyItem(String id , String name , String description , String unlockDirection ) {
        super(id , name , description ); // Temel sınıfın constructor'ını çağırır.
        this.unlockDirection = unlockDirection ; // Açacağı yönü atar.
    }
    // Anahtar kullanıldığında çalışacak metot (Item'dan override edilir).
    @Override
    public void onUse(Player p , GameEngine ctx ) {
        Room current = p.getCurrentRoom(); // Oyuncunun bulunduğu odayı alır.
        // Odanın 'unlockDirection' metodunu çağırarak belirtilen yöndeki kilidi açmayı dener.
        if(current.unlockDirection(unlockDirection)) {
            // Eğer kilit başarıyla açılırsa (metot true döndürürse) mesaj yazdırılır.
            System.out.println(name + " kullanıldı. " + unlockDirection + " yönündeki kapı açıldı!");
        } else {
            // Kapı zaten açıksa veya o yönde kilit yoksa oyuncuya bilgi verilir.
            System.out.println(name + " kullanıldı, fakat açılacak bir kapı yok veya zaten açık.");
        }
    }
}
// ====================================================================
// Can İksiri
// ====================================================================
// Kullanıldığında oyuncunun canını artıran eşya sınıfı.
class PotionItem extends Item {
    private int healthBoost; // Kaç puan can artışı sağlayacağını saklar.
    // Constructor , temel özellikleri ve can artış miktarını belirler.
    public PotionItem(String id , String name , String description , int healthBoost ) {
        super(id , name , description ); // Temel sınıfın constructor'ını çağırır.
        this.healthBoost = healthBoost ; // Can artış miktarını atar.
    }
    // İksir kullanıldığında çalışacak metot.
    @Override
    public void onUse(Player p , GameEngine ctx ) {
        p.setHp(p.getHp() + healthBoost); // Oyuncunun mevcut canına can artış miktarını ekler.
        System.out.println("Can iksiri kullanıldı. Canın " + healthBoost + " arttı. Yeni Can: " + p.getHp());
        p.getInventory().remove(this); // İksir tek kullanımlık olduğu için envanterden çıkarılır.
    }
}
// ====================================================================
// Silah Eşyası
// ====================================================================
// Kullanıldığında oyuncuya saldırı avantajı sağlayan eşya sınıfı (örnek: kılıç).
class WeaponItem extends Item {
    // Constructor, temel özellikleri atar.
    public WeaponItem(String id , String name , String description ) { super(id , name , description ); }
    // Silah kullanıldığında (kuşanıldığında) çalışacak metot.
    @Override
    public void onUse(Player p , GameEngine ctx ) {
        // Basitçe bir mesaj yazdırılır, oyunda kalıcı bir güç artışı gibi bir etki uygulanabilir.
        System.out.println(this.name + " kuşanıldı. Saldırı gücün arttı.");
    }
}
// ====================================================================
// 2. NPC Sınıfları
// ====================================================================
// Oyunda karşılaşılan tüm karakterler (NPC: Non-Player Character) için temel sınıf.
class NPC {
    protected String name; // NPC'nin adını saklar.
    // Constructor, NPC adını atar.
    public NPC(String name ) { this.name = name ; }
    // NPC'nin adını döndürür.
    public String getName() { return name; }
    // Konuşma metodu, oyuncu 'talk' komutu verdiğinde çalışır. Alt sınıflarda özelleştirilebilir.
    public void respondToTalk() { System.out.println(name + ": Merhaba, konuşmaya hazırım."); }
    // Yeni: Scanner parametresi alan versiyon (varsayılan olarak eskiyi çağırır).
    public void respondToTalk(Scanner sc) { respondToTalk(); }
}
// ====================================================================
// Konuşma düğümleri
// ====================================================================
// NPC ile konuşma sırasında bir diyalog parçasını ve olası oyuncu seçimlerini saklayan sınıf.
class ConversationNode {
    private String text; // Görüntülenecek diyalog metnini tutar.
    private List<ConversationChoice> choices = new ArrayList<>(); // Oyuncunun seçebileceği seçenekleri tutar.
    // Constructor, düğüm metnini atar.
    public ConversationNode(String text ) { this.text = text ; }
    // Düğüme yeni bir seçenek ekler.
    public void addChoice(ConversationChoice choice ) { choices.add(choice ); }
    // Düğüm metnini döndürür.
    public String getText() { return text; }
    // Seçenek listesini döndürür.
    public List<ConversationChoice> getChoices() { return choices; }
}
// Konuşma sırasında oyuncunun seçebileceği tek bir seçeneği temsil eden sınıf.
class ConversationChoice {
    private String text; // Seçeneğin görünen metnini tutar.
    private ConversationNode next; // Bu seçim yapıldıktan sonra geçilecek bir sonraki düğümü tutar (null ise konuşma biter).
    // Constructor, seçenek metnini ve bir sonraki düğümü atar.
    public ConversationChoice(String text , ConversationNode next ) { this.text = text ; this.next = next ; }
    // Seçenek metnini döndürür.
    public String getText() { return text; }
    // Bir sonraki ConversationNode nesnesini döndürür.
    public ConversationNode getNext() { return next; }
}
// ====================================================================
// Dost NPC
// ====================================================================
// Oyuncuya ipucu veren, konuşma dizisi (diyalog ağacı) olan NPC sınıfı.
class FriendlyNPC extends NPC {
    private ConversationNode rootNode; // Konuşmanın başlangıç düğümünü tutar.
    // Constructor, temel NPC adını atar ve konuşma ağacını başlatır.
    public FriendlyNPC(String name ) {
        super(name ); // Temel sınıfın constructor'ını çağırır.
        initConversation(); // Konuşma ağacını oluşturur.
    }
    // Konuşma ağacını (diyalog düğümlerini ve seçimlerini) oluşturur.
    private void initConversation() {
        // İlk düğüm oluşturulur.
        rootNode = new ConversationNode("Merhaba maceracı! Sana ipucu verebilirim.");
        // İkinci düğüm oluşturulur.
        ConversationNode node2 = new ConversationNode("Doğu yönündeki kapıyı açmak için kırmızı anahtar lazım.");
        // İlk düğüme 'Devam et' seçeneği eklenir, bu seçenek seçilirse node2'ye geçilir.
        rootNode.addChoice(new ConversationChoice("Devam et", node2));
        // İkinci düğüme 'Teşekkür ederim' seçeneği eklenir, bu seçenek seçilirse 'null'a geçilerek konuşma sonlanır.
        node2.addChoice(new ConversationChoice("Teşekkür ederim", null)); // Son düğüm (sonraki null).
    }
    // Oyuncu konuştuğunda çağrılan ve diyalogu yöneten metot. Scanner artık dışarıdan verilir.
    @Override
    public void respondToTalk(Scanner sc) {
        ConversationNode current = rootNode; // Konuşmayı başlangıç düğümünden başlatır.
        while (current != null) { // Mevcut düğüm null olmadığı sürece (konuşma bitmediği sürece) döner.
            System.out.println(getName() + ": " + current.getText()); // NPC'nin mevcut diyalog metnini yazdırır.
            List<ConversationChoice> choices = current.getChoices(); // Mevcut düğümün seçeneklerini alır.
            if (choices.isEmpty()) break; // Seçenek yoksa konuşmayı bitirir.
            // Seçenekleri numaralandırarak oyuncuya gösterir.
            for (int i = 0; i < choices.size(); i++) System.out.println((i + 1) + ") " + choices.get(i).getText());
            System.out.print("> Seçim: "); // Oyuncudan seçim girmesini ister.
            String input = sc.nextLine(); // Gelen scanner kullanılarak girdiyi okur (yeni Scanner oluşturulmuyor).
            int choiceIndex = 0; // Seçim indeksini tutacak değişken.
            try {
                choiceIndex = Integer.parseInt(input) - 1; // Girdiyi sayıya çevirir ve 0-tabanlı indekse dönüştürür.
                // İndeksin geçerli aralıkta olup olmadığını kontrol eder.
                if (choiceIndex < 0 || choiceIndex >= choices.size()) break;
            }
            catch (Exception e ) { break; } // Sayısal olmayan veya hatalı girdi durumunda konuşmayı bitirir.
            current = choices.get(choiceIndex).getNext(); // Seçilen seçeneğin bir sonraki düğümüne geçer.
        }
        System.out.println(getName() + " ile konuşma sona erdi."); // Döngü bittiğinde (konuşma sonlandığında) mesaj yazdırır.
    }
}
// ====================================================================
// Düşman NPC
// ====================================================================
// Oyuncuya zarar verebilen, düşmanca NPC sınıfı.
class EnemyNPC extends NPC {
    private int damage; // Saldırı gücünü saklar.
    // Constructor, ad ve hasar miktarını atar.
    public EnemyNPC(String name , int damage ) {
        super(name ); // Temel sınıfın constructor'ını çağırır.
        this.damage = damage ; // Hasar miktarını atar.
    }
    // Oyuncuya saldırma metodu.
    public void attack(Player p ) {
        if(p != null) { // Oyuncu nesnesi varsa
            p.setHp(Math.max(0, p.getHp() - damage)); // Oyuncunun canını hasar miktarı kadar azaltır, 0'ın altına düşürme.
            System.out.println(getName() + " saldırdı ve " + damage + " hasar verdi. Kalan Can: " + p.getHp()); // Saldırı sonucunu yazdırır.
        }
    }
    // Düşman NPC'nin konuşma metodu (genellikle kısa ve düşmanca).
    @Override
    public void respondToTalk() { System.out.println(getName() + ": Yoldan çekil!"); }
}
// ====================================================================
// 3. Room ve Player
// ====================================================================
// Oda sınıfı, oyun dünyasındaki bir konumu temsil eder.
class Room {
    private String id, name, description; // Kimlik, ad ve açıklama.
    private Map<String, Room> exits = new HashMap<>(); // Odanın çıkışlarını (yön -> komşu oda) saklar.
    private Set<String> lockedDirections = new HashSet<>(); // Kilitli olan yönleri (ör. "doğu") saklar.
    private List<Item> items = new ArrayList<>(); // Oda içinde bulunan eşyalar.
    private List<NPC> npcs = new ArrayList<>(); // Oda içinde bulunan NPC'ler.
    // Constructor, odanın temel özelliklerini atar.
    public Room(String id , String name , String description ) { this.id=id ; this.name=name ;
        this.description=description ; }
    // Bir komşu odayla bağlantı kurar.
    public void connect(String dir , Room neighbor ) { exits.put(dir , neighbor ); }
    // Belirtilen yöndeki komşu odayı döndürür.
    public Room getExit(String dir ) { return exits.get(dir ); }
    // Belirtilen yönü kilitli hale getirir (Set'e ekler).
    public void lockDirection(String dir ) { lockedDirections.add(dir ); }
    // Belirtilen yöndeki kilidi açar (Set'ten çıkarır) ve başarılı olup olmadığını döndürür.
    public boolean unlockDirection(String dir ) { return lockedDirections.remove(dir ); }
    // Odaya eşya ekler.
    public void addItem(Item item ) { items.add(item ); }
    // Odaya NPC ekler.
    public void addNPC(NPC npc ) { npcs.add(npc ); }
    // Adıyla odadaki bir eşyayı bulur (büyük/küçük harf duyarsız).
    public Item getItem(String name ) { for(Item i:items) if(i.getName().equalsIgnoreCase(name )) return i; return null;
    }
    // Adıyla odadaki bir NPC'yi bulur (büyük/küçük harf duyarsız).
    public NPC getNPC(String name ) { for(NPC n:npcs) if(n.getName().equalsIgnoreCase(name )) return n; return
            null; }
    // Oda detaylarını (ad, açıklama, çıkışlar, eşyalar, karakterler) oyuncuya yazdırır.
    public void describe() {
        System.out.println(" (Room ID: " + id + ")"); // Oda kimliğini yazdırır (geliştirme/debug amaçlı).
        System.out.println("\n=== Şu anda bulunduğun yer: " + name + " ==="); // Oda adını vurgular.
        System.out.println(description); // Açıklamayı yazdırır.
        // Çıkış yönlerini listeler (varsa).
        System.out.print("Çıkışlar: " + (exits.isEmpty()? "yok":String.join(", ", exits.keySet()))); System.out.println();
        // Odadaki eşyaları listeler (varsa). Yardımcı metot 'getNames' kullanılır.
        System.out.print("Eşyalar: " + (items.isEmpty()? "yok":getNames(items))); System.out.println();
        // Odadaki karakterleri (NPC'leri) listeler (varsa). Yardımcı metot 'getNames' kullanılır.
        System.out.print("Karakterler: " + (npcs.isEmpty()? "yok":getNames(npcs))); System.out.println();
    }
    // Verilen listedeki (Item veya NPC) nesnelerin adlarını birleştirerek String olarak döndürür.
    private String getNames(List<?> list ) {
        List<String> names = new ArrayList<>(); // Adları toplamak için liste.
        for(Object o:list ) {
            if(o instanceof Item) names.add(((Item)o).getName()); // Eşya ise adını ekle.
            if(o instanceof NPC) names.add(((NPC)o).getName()); // Eğer NPC'yse adını ekle.
        }
        return String.join(", ", names); // Adları virgül ve boşlukla birleştirip döndür.
    }
    // Oda adını döndürür.
    public String getName() { return name; }
    // Odadaki eşya listesini döndürür.
    public List<Item> getItems() { return items; }
    // Belirtilen yönün kilitli olup olmadığını kontrol eder.
    public boolean isLocked(String dir ) { return lockedDirections.contains(dir ); }
}
// ====================================================================
// Oyuncu sınıfı
// ====================================================================
class Player {
    private Room currentRoom; // Oyuncunun o anda bulunduğu oda.
    private List<Item> inventory = new ArrayList<>(); // Oyuncunun envanteri (sahip olduğu eşyalar).
    private int hp; // Can puanı (Health Points).
    // Constructor, oyuncuyu başlangıç odasına yerleştirir ve canını 100 olarak ayarlar.
    public Player(Room startingRoom ) { this.currentRoom = startingRoom ; this.hp=100; }
    // Oyuncu hareketi komutunu işler.
    public void move(String dir , GameEngine ctx ) {
        // Gidilmek istenen yön kilitli mi diye kontrol eder.
        if(currentRoom.isLocked(dir )) { System.out.println("Kapı kilitli! Anahtar kullanmalısın."); return; }
        Room next = currentRoom.getExit(dir ); // Gidilmek istenen yöndeki odayı alır.
        if(next!=null) {
            currentRoom=next; // Oyuncunun odasını değiştirir.
            currentRoom.describe(); // Yeni odanın açıklamasını yazdırır.
        }
        else System.out.println("Bu yönde çıkış yok."); // O yönde çıkış yoksa mesaj yazdırır.
    }
    // Eşya alma komutunu işler.
    public void take(String itemName , GameEngine ctx ) {
        Item item = currentRoom.getItem(itemName ); // Odada o isimde eşya var mı kontrol eder.
        if(item!=null) {
            currentRoom.getItems().remove(item); // Odadan eşyayı kaldırır.
            inventory.add(item); // Envantere ekler.
            System.out.println(item.getName()+" aldın."); // Bilgilendirme mesajı.
        }
        else System.out.println("Böyle bir eşya bulunmuyor."); // Odada eşya yoksa mesaj.
    }
    // Eşya kullanma komutunu işler.
    public void use(String itemName , GameEngine ctx ) {
        Item item = null;
        // Envanterde eşyayı arar.
        for(Item i:inventory) if(i.getName().equalsIgnoreCase(itemName )) { item=i; break; }
        if(item!=null) item.onUse(this, ctx ); // Eşya varsa 'onUse' metodunu çağırır.
        else System.out.println("Bu eşya sende yok."); // Eşya envanterde yoksa mesaj.
    }
    // Envanteri ve can puanını yazdırır.
    public void inventoryText() {
        System.out.print("Envanter (Can:"+hp+"): ");
        if(inventory.isEmpty()) System.out.println("Boş.");
        else {
            List<String> names=new ArrayList<>();
            for(Item i:inventory) names.add(i.getName()); // Envanterdeki eşyaların adlarını toplar.
            System.out.println(String.join(", ", names)); // Adları birleştirip yazdırır.
        }
    }
    // Can puanını döndürür.
    public int getHp() { return hp; }
    // Can puanını ayarlar.
    public void setHp(int hp ) { this.hp=hp ; }
    // Oyuncunun bulunduğu odayı döndürür.
    public Room getCurrentRoom() { return currentRoom; }
    // Envanter listesini döndürür.
    public List<Item> getInventory() { return inventory; }
}
// ====================================================================
// 4. GameEngine
// ====================================================================
// Oyunun ana döngüsünü ve mantığını yöneten sınıf.
class GameEngine {
    private Player player; // Oyuncu nesnesi.
    private Scanner scanner = new Scanner(System.in); // Konsoldan komut girişişi için tek Scanner.
    // Oyunun olası durumları (başlangıç, çalışıyor, son).
    private enum GameState{INIT,RUNNING,END}
    private GameState state = GameState.INIT; // Başlangıç durumu.
    // Oyun haritasını, eşyaları, NPC'leri oluşturup oyunu başlatır.
    public void initGame() {
        // Odalar oluşturulur (ID, Ad, Açıklama).
        Room salon = new Room("r1","Salon","Geniş bir salon. Kırmızı bir kapı dikkatini çekiyor.");
        Room mutfak = new Room("r2","Mutfak","Kirli tabaklarla dolu bir mutfak.");
        Room silahOdasi = new Room("r3","Silah Odası","Duvarlarda kılıçlar asılı. Silahlar var.");
        Room koridor = new Room("r4","Uzun Koridor","Loş bir koridor. Batı'ya giden bir ışık var.");
        Room cikis = new Room("r5","Çıkış Kapısı","Oyunun sonu. Buradan dışarı çıkabilirsin!");
        // Odaları birbirine bağlar (Yön, Komşu Oda).
        salon.connect("doğu", silahOdasi); salon.connect("güney", mutfak);
        mutfak.connect("kuzey", salon);
        silahOdasi.connect("batı", salon); silahOdasi.connect("kuzey", koridor);
        koridor.connect("güney", silahOdasi); koridor.connect("batı", cikis);
        cikis.connect("doğu", koridor);
        // Kilitli yönler belirlenir.
        salon.lockDirection("doğu"); // Salon'dan Silah Odası'na geçiş kilitler (kırmızı anahtarla açılacak).
        // Eşyalar oluşturulur ve odalara eklenir.
        salon.addItem(new KeyItem("k1","kırmızı anahtar","Kırmızı kapı kilidini açar.","doğu")); // Anahtar eşyası (doğu yönünü açar).
        mutfak.addItem(new PotionItem("p1","can iksiri","Can puanı verir.",20)); // İksir eşyası (20 can verir).
        silahOdasi.addItem(new WeaponItem("w1","kılıç","Güçlü bir kılıç.")); // Silah eşyası.
        // NPC'ler oluşturulur ve odalara eklenir.
        salon.addNPC(new FriendlyNPC("Muhafız")); // Dost NPC.
        koridor.addNPC(new EnemyNPC("Dev Örümcek",15)); // Düşman NPC (15 hasar verir).
        player=new Player(salon); // Oyuncuyu başlangıç odasına (Salon) yerleştirir.
        state=GameState.RUNNING; // Oyun durumunu 'çalışıyor' olarak ayarlar.
    }
    // Oyunun ana döngüsünü çalıştırır.
    public void runGame() {
        System.out.println("\nOyun Başladı. 'help' yazarak komutları görebilirsin.");
        player.getCurrentRoom().describe(); // Başlangıç odasını tanımlar.
        // Oyun 'çalışıyor' durumunda olduğu ve oyuncunun canı sıfırdan büyük olduğu sürece döngü devam eder.
        while(state==GameState.RUNNING && player.getHp()>0){
            System.out.print("\n> "); // Komut istemi.
            String fullCommand=scanner.nextLine().trim().toLowerCase(); // Kullanıcı girdisini okur, boşlukları temizler ve küçük harfe çevirir.
            String[] parts=fullCommand.split(" ",2); // Komutu (parts[0]) ve varsa argümanı (parts[1]) ayırır.
            String command=parts[0]; // Komut.
            String arg=parts.length>1? parts[1].trim():""; // Argüman (yoksa boş string).
            processCommand(command,arg); // Komutu işler.
            // Oyun sonu kontrolleri:
            if(player.getHp()<=0){System.out.println("Canın bitti. Oyun Bitti!"); state=GameState.END;} // Can sıfır veya altındaysa oyun biter.
            // Oyuncu 'Çıkış Kapısı' odasındaysa oyun biter.
            else if(player.getCurrentRoom().getName().equals("Çıkış Kapısı")){System.out.println("Tebrikler! Oyunu\nbaşarıyla tamamladın!"); state=GameState.END;}
        }
        System.out.println("\n=== Oyun Sonu Özeti ===");
        player.inventoryText(); // Oyun bittiğinde envanteri ve son can durumunu gösterir.
        
    }
    // Kullanıcıdan gelen komutları ayrıştırır ve ilgili işlemleri çağırır.
    private void processCommand(String command , String arg ){
        try{ // Hata yakalama bloğu.
            switch(command ){
                case "look": player.getCurrentRoom().describe(); break; // Odayı tekrar tanımla.
                case "go": if(!arg .isEmpty()) player.move(arg ,this); else System.out.println("Hangi yöne?"); break; // Hareket et.
                case "take": if(!arg .isEmpty()) player.take(arg ,this); else System.out.println("Neyi alacaksın?"); break; // Eşya al.
                case "use": if(!arg .isEmpty()) player.use(arg ,this); else System.out.println("Neyi kullanacaksın?"); break; // Eşya kullan.
                case "talk": handleTalk(arg ); break; // NPC ile konuş.
                case "inv": player.inventoryText(); break; // Envanteri göster.
                case "help": showHelp(); break; // Yardım menüsünü göster.
                case "quit": state=GameState.END; System.out.println("Oyundan çıkılıyor..."); break; // Oyunu sonlandır.
                case "say": System.out.println("Sen: "+arg ); break; // Oyuncunun basitçe konuşması.
                default: System.out.println("Bilinmeyen komut."); break; // Bilinmeyen komut.
            }
        }catch(Exception e ){ System.out.println("Komut işlenirken hata oluştu."); } // Hata olursa genel mesaj.
    }
    // 'talk [karakter]' komutunu işler.
    private void handleTalk(String npcName ){
        NPC npc = player.getCurrentRoom().getNPC(npcName ); // Odadaki NPC'yi adına göre bulur.
        if(npc!=null){
            npc.respondToTalk(scanner); // Tek scanner burada kullanılıyor
            // Eğer NPC bir Düşman NPC ise, konuşmanın hemen ardından saldırı metodunu çağırır.
            if(npc instanceof EnemyNPC) ((EnemyNPC)npc).attack(player);
        } else System.out.println("Böyle bir karakter bulunmuyor."); // NPC bulunamazsa mesaj.
    }
    // Oyuncuya kullanılabilir komutları gösteren yardım menüsü.
    private void showHelp(){
        System.out.println("\n--- Komut Listesi ---\n");
        System.out.println("look : Odanın açıklamasını gör.");
        System.out.println("go [yön] : Yön belirt (kuzey/güney/doğu/batı).");
        System.out.println("take [eşya] : Odadaki eşyayı al.");
        System.out.println("use [eşya] : Envanterdeki eşyayı kullan.");
        System.out.println("talk [karakter] : Karakterle konuş.");
        System.out.println("inv : Envanter ve can.");
        System.out.println("say [metin] : Konuşma.");
        System.out.println("help : Komut listesi.");
        System.out.println("quit : Oyunu sonlandır.");
    }
}
// ====================================================================
// 5. Main
// ====================================================================
// Ana program sınıfı.
public class MiniMaceraOyunu {
    // Programın başlangıç noktası.
    public static void main(String[] args ) {
        GameEngine engine = new GameEngine(); // Oyun motoru nesnesi oluşturulur.
        System.out.println("=== YMÜ227 Mini Macera Oyunu ==="); // Oyun başlığı.
        engine.initGame(); // Oyunu başlatır (harita, eşyalar, NPC'ler oluşturulur).
        engine.runGame(); // Oyunun ana döngüsünü çalıştırır.
    }
}
