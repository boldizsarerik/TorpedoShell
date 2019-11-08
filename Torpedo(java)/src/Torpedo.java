import java.util.*;
import shell.Shell;
import shell.Command;
import shell.Status;
import shell.Main;
import shell.Result;

public class Torpedo extends Shell{
    int rockets;
    int[] hajok = new int[7];
    //List<Integer> hajok = new ArrayList<Integer>();
    boolean inic;;
    Status[][] tabla = new Status[10][10]; // a név után is lehet rakni a szögletes zárójelpárokat
    Map<Character, Integer> oszlopok = new Hashtable<>(); // TreeMap is lehetne
    
    class loves implements Comparable<loves> {
    	public Character oszlop;
    	public int sor;
		
    	public loves(Character oszlop, int sor) {
            this.oszlop = oszlop;
            this.sor = sor;
        }

        @Override
        public int compareTo(loves o) {
            if(this.oszlop == o.oszlop && this.sor == o.sor)
                return 0;
            else
                return 1;
        }    	
    }
    
    Set<loves> lovesek;// = new TreeSet<>();
    { //statikus inicializáló blokk; a program indításakor lefut
    	inic = false;
    	for(int i = 1; i <= 10; i++) {    		
    		//System.out.print((char)(i+64) + ":" + i + '|');
    		oszlopok.put((char)(i+64), i);  // (A, 1) , (B, 2) ... (key, value)
    	}
    }
	
    boolean vanmeg() { // van még hajó ?
    	boolean van = false;		
       
        for (int i : hajok) { 
            if(i != 0)     // itt ellenőrzöm, hogy maradt-e nem nulla hosszúságó hajó még (vagyis, hogy van-e még hajó)
                van = true;
        }		
        if(van)  // elég lenne annyi, hogy return van;
            return true;
        else
            return false;
    }
    
    void Print() {      // kiírja minden aktuális paraméterát a játéknak (rakéták száma, a tábla mezőinek státusza, stb.)
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                        //format("%s", curr.name());
                        format("%6s", tabla[i][j]/*.name()*/);
                }
                format("%n"); // sortörés
            }
            format("Hátralévő rakéták: %d %n", rockets);
            format("Megkeresendő hajók:");
            for (int i : hajok) {
                if(i != 0)
                    format(" %d", i);
            }
            format("%n");

            if(rockets == 0 || !vanmeg())
                format("A játék véget ért!");
            else
                format("A játék még nem ért véget!");
            format("%n");		
    }
	
    public Torpedo() {
      
        addCommand(new Command("new") {

            @Override
            public boolean execute(String... arg0) {			
                //System.out.println("ðŸš¢");
                int szam;
                if(arg0.length > 1)
                    return false;

                if(arg0.length == 1) {
                    try {
                        szam = Integer.parseInt(arg0[0]);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    rockets = szam;
                }
                else
                    rockets = 15;

                lovesek = new TreeSet<>();
                hajok[0] = 1; hajok[1] = 1; hajok[2] = 2; hajok[3] = 2; hajok[4] = 3;  hajok[5] = 4; hajok[6] = 5;				

                for (int i = 0; i < 10; i++) 
                    for (int j = 0; j < 10; j++) 
                        tabla[i][j] = Status.WATER;

                init(); // enélkül is működik
                inic = true;
                Print();

                return true;
            }
        });

        addCommand(new Command("print") {

            @Override
            public boolean execute(String... arg0) {
                if(arg0.length != 0 || !inic)
                    return false;

                Print();								
                return true;
            }
        });

        addCommand(new Command("fire") {

            @Override
            public boolean execute(String... arg0) {
                if(arg0.length != 2 || !inic || rockets == 0 || !vanmeg())
                    return false;
                Character oszlop = arg0[0].charAt(0);
                if(!(oszlop.equals('A') || oszlop.equals('B') || oszlop.equals('C') || oszlop.equals('D') || oszlop.equals('E') 
                    || oszlop.equals('F') || oszlop.equals('G') || oszlop.equals('H') || oszlop.equals('I') || oszlop.equals('J')))
                        return false;
                int sor;
                try {			
                    sor = Integer.parseInt(arg0[1]);
                } catch (NumberFormatException e) {
                    return false;
                }				
                if(!(sor >= 1 && sor <= 10))
                    return false;

                loves ez = new loves(oszlop, sor);
                boolean votma = false; // nem is használja sehol ezt a változót
                for (loves akt : lovesek) {
                    if(lovesek.contains(ez))
                        return false;
                }
                //----------------feltételek-vége---------------------

                lovesek.add(ez);
                rockets--;
                Result eredmeny = resultOfShot(sor-1, oszlopok.get(oszlop)-1); 
                //System.out.println(eredmeny.getRow());
                //System.out.println(eredmeny.getColumn());
                //System.out.println(eredmeny.getSize());
                //System.out.println(eredmeny.isHorizontal());
                if(eredmeny.isHit()) {
                    rockets += 2;
                    tabla[sor-1][oszlopok.get(oszlop)-1] = Status.SHIP;

                    if(eredmeny.isSank()) {
                        rockets += eredmeny.getSize();
                        if(!eredmeny.isHorizontal()) { // ha függőlegesen helyezkedik el a hajó, ez a blokk fut le
                            for(int i = eredmeny.getRow(); i < eredmeny.getRow() + eredmeny.getSize(); i++)
                                tabla[i][eredmeny.getColumn()] = Status.SANK;
                        }
                        else
                            for(int i = eredmeny.getColumn(); i < eredmeny.getColumn() + eredmeny.getSize(); i++)
                                tabla[eredmeny.getRow()][i] = Status.SANK;

                        int k = 0;
                        for (int i = 0; i < 7; i++) {
                            if(hajok[i] == eredmeny.getSize())
                                k = i;
                        } // úgy is lehetne egyszerűbben, hogy miután megegyezik a hajó hosszával a tömb egyik eleme, lenullázzuk és break-kel kilépünk a forból 
                        hajok[k] = 0; // így pedig az adott hossz utolsó előfordulásának indexén nullázza le
                    }
                }
                else 
                    tabla[sor-1][oszlopok.get(oszlop)-1] = Status.HIT;

                Print();
                return true;
            }
        });
    }

    public static void main(String[] args){
        Shell sh = Loader.load();
        sh.readEvalPrint();   
    }
}