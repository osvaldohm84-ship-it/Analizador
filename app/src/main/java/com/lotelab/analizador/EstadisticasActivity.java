package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.view.*;import android.widget.*;import android.graphics.*;import java.util.*;
public class EstadisticasActivity extends Activity{
EditText fechaDesde,fechaHasta;
Button btnAnalizar,btnOrdenar,btnVer;
TableLayout tablaStats;
TextView contadorStats;
List<Draw> todos=new ArrayList<>();
int categoria=0;
int filtroPosicion=0;
int ordenActual=2;
int numSorteos=0;
static final String[] CATEGORIAS={"Números","Centenas","Decenas","Unidades","Dígitos","Parlets"};
static final String[] FILTROS_POS={"Todas","Solo Fijo","Solo C1","Solo C2","Solo Centenas","Solo Corridos"};
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_estadisticas);
fechaDesde=findViewById(R.id.fechaDesde);
fechaHasta=findViewById(R.id.fechaHasta);
btnAnalizar=findViewById(R.id.btnAnalizar);
btnOrdenar=findViewById(R.id.btnOrdenar);
btnVer=findViewById(R.id.btnVer);
tablaStats=findViewById(R.id.tablaStats);
contadorStats=findViewById(R.id.contadorStats);
new Thread(()->{
todos=leerTodos();
runOnUiThread(()->{
if(todos.size()>0){fechaDesde.setText(fechaMin());fechaHasta.setText(fechaMax());}
});
}).start();
btnAnalizar.setOnClickListener(v->analizar());
btnOrdenar.setOnClickListener(v->elegirOrden());
btnVer.setOnClickListener(v->elegirCategoria());
}
void elegirCategoria(){
new AlertDialog.Builder(this).setTitle("Ver categoría:").setItems(CATEGORIAS,(d,w)->{
categoria=w;
if(categoria==4){elegirFiltroPosicion();}else{filtroPosicion=0;btnVer.setText("Ver: "+CATEGORIAS[categoria]);analizar();}
}).show();
}
void elegirFiltroPosicion(){
new AlertDialog.Builder(this).setTitle("Filtro de posición (Dígitos):").setItems(FILTROS_POS,(d,w)->{
filtroPosicion=w;
btnVer.setText("Ver: Dígitos/"+FILTROS_POS[w]);
analizar();
}).show();
}
void elegirOrden(){
String[] opciones;
switch(categoria){
case 0:opciones=new String[]{"Número","Salidas","Frecuencia","Como Fijo","Como Corrido","Tarde","Noche"};break;
case 1:case 2:case 3:opciones=new String[]{"Dígito","Salidas","Frecuencia","Tarde","Noche","Porcentaje"};break;
case 4:opciones=new String[]{"Dígito","Apariciones","Frecuencia","Porcentaje"};break;
case 5:opciones=new String[]{"Parlet","Apariciones","Frecuencia","Tarde","Noche"};break;
default:opciones=new String[]{"Pos"};break;
}
new AlertDialog.Builder(this).setTitle("Ordenar por:").setItems(opciones,(d,w)->{ordenActual=w;analizar();}).show();
}
void analizar(){
if(todos.isEmpty()){Toast.makeText(this,"Cargando...",Toast.LENGTH_SHORT).show();return;}
String desde=fechaDesde.getText().toString().trim();
String hasta=fechaHasta.getText().toString().trim();
List<Draw> filtrados=new ArrayList<>();
for(Draw d:todos){
if(!desde.isEmpty()&&comparar(d.date,desde)<0)continue;
if(!hasta.isEmpty()&&comparar(d.date,hasta)>0)continue;
filtrados.add(d);
}
if(filtrados.isEmpty()){Toast.makeText(this,"No hay sorteos",Toast.LENGTH_SHORT).show();tablaStats.removeAllViews();contadorStats.setText("0");return;}
numSorteos=filtrados.size();
new Thread(()->{
switch(categoria){
case 0:calcNumeros(filtrados);break;
case 1:calcDigitosCat(filtrados,1);break;
case 2:calcDigitosCat(filtrados,2);break;
case 3:calcDigitosCat(filtrados,3);break;
case 4:calcDigitosTodos(filtrados);break;
case 5:calcParlets(filtrados);break;
}
}).start();
}
void calcNumeros(List<Draw> filt){
Est[] ests=new Est[100];
for(int i=0;i<100;i++){ests[i]=new Est();ests[i].id=i;}
for(Draw d:filt){
int[] vals={parse(d.f),parse(d.c1),parse(d.c2)};
for(int k=0;k<3;k++){int x=vals[k];if(x>=0&&x<100){ests[x].veces++;if(k==0)ests[x].coFij++;else ests[x].coCor++;if(d.tn.equals("T"))ests[x].tar++;else ests[x].noc++;}}
}
List<Est> lista=new ArrayList<>(Arrays.asList(ests));
ordenarEst(lista);
runOnUiThread(()->dibujarNumeros(lista));
}
void calcDigitosCat(List<Draw> filt,int pos){
int[] cnt=new int[10];int[] tar=new int[10];int[] noc=new int[10];
for(Draw d:filt){
String val=(pos==1)?d.cent:(pos==2)?d.f:d.c1;
int x=parse(val);
if(x<0)continue;
int dig=(pos==1)?x:(pos==2)?x:(pos==3)?x:x;
if(pos==1)dig=x;
else dig=(pos==2)?(x):(x);
if(pos==1)dig=x;
if(pos==2)dig=(x>=0)?x:-1;
if(pos==3)dig=x;
if(pos==1)dig=x;
if(dig<0)continue;
if(pos==1)cnt[dig]++;
else if(pos==2){cnt[x]++;if(d.tn.equals("T"))tar[x]++;else noc[x]++;}
else{cnt[x]++;if(d.tn.equals("T"))tar[x]++;else noc[x]++;}
}
List<Est> lista=new ArrayList<>();
for(int i=0;i<10;i++){Est e=new Est();e.id=i;e.veces=cnt[i];e.tar=tar[i];e.noc=noc[i];lista.add(e);}
final List<Est> fl=lista;
runOnUiThread(()->dibujarDigitosCat(fl));
}
void calcDigitosTodos(List<Draw> filt){
int[] cnt=new int[10];
for(Draw d:filt){
List<String> vals=new ArrayList<>();
switch(filtroPosicion){
case 0:vals.add(d.cent);vals.add(d.f);vals.add(d.c1);vals.add(d.c2);break;
case 1:vals.add(d.f);break;
case 2:vals.add(d.c1);break;
case 3:vals.add(d.c2);break;
case 4:vals.add(d.cent);break;
case 5:vals.add(d.c1);vals.add(d.c2);break;
}
for(String v:vals){
if(v==null||v.isEmpty())continue;
for(char c:v.toCharArray()){if(Character.isDigit(c))cnt[c-'0']++;}
}
}
List<Est> lista=new ArrayList<>();
for(int i=0;i<10;i++){Est e=new Est();e.id=i;e.veces=cnt[i];lista.add(e);}
final List<Est> fl=lista;
runOnUiThread(()->dibujarDigitosTodos(fl));
}
void calcParlets(List<Draw> filt){
Map<String,int[]> mapa=new HashMap<>();
for(Draw d:filt){
int f=parse(d.f);int c1=parse(d.c1);int c2=parse(d.c2);
if(f<0||c1<0||c2<0)continue;
String[] pares={par(f,c1),par(f,c2),par(c1,c2)};
for(String p:pares){
if(!mapa.containsKey(p))mapa.put(p,new int[3]);
mapa.get(p)[0]++;
if(d.tn.equals("T"))mapa.get(p)[1]++;else mapa.get(p)[2]++;
}
}
List<Par> lista=new ArrayList<>();
for(Map.Entry<String,int[]> e:mapa.entrySet()){lista.add(new Par(e.getKey(),e.getValue()[0],e.getValue()[1],e.getValue()[2]));}
Collections.sort(lista,(a,b)->b.veces-a.veces);
runOnUiThread(()->dibujarParlets(lista));
}
String par(int a,int b){return a<=b?String.format("%02d-%02d",a,b):String.format("%02d-%02d",b,a);}
void ordenarEst(List<Est> lista){
switch(ordenActual){
case 0:Collections.sort(lista,(a,b)->a.id-b.id);break;
case 1:Collections.sort(lista,(a,b)->b.veces-a.veces);break;
case 2:Collections.sort(lista,(a,b)->frecComp(b.veces,a.veces));break;
case 3:Collections.sort(lista,(a,b)->b.coFij-a.coFij);break;
case 4:Collections.sort(lista,(a,b)->b.coCor-a.coCor);break;
case 5:Collections.sort(lista,(a,b)->b.tar-a.tar);break;
case 6:Collections.sort(lista,(a,b)->b.noc-a.noc);break;
}
}
int frecComp(int a,int b){if(a==0&&b==0)return 0;if(a==0)return 1;if(b==0)return -1;return b-a;}
void dibujarNumeros(List<Est> lista){
tablaStats.removeAllViews();tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] h={"Pos","#","Sal","Frec","Fij","Cor","Tar","Noc"};
TableRow enc=new TableRow(this);
for(String s:h)enc.addView(celda(s,"#1565C0",Color.WHITE,true));
tablaStats.addView(enc);
for(int i=0;i<lista.size();i++){
Est e=lista.get(i);
TableRow fila=new TableRow(this);
String bg=(i%2==0)?"#FFFFFF":"#F5F5F5";
fila.addView(celda(String.valueOf(i+1),bg,Color.parseColor("#1A237E"),true));
fila.addView(celda(String.format("%02d",e.id),bg,Color.parseColor("#0D47A1"),true));
fila.addView(celda(String.valueOf(e.veces),bg,Color.parseColor("#1B5E20"),false));
fila.addView(celda(frec(e.veces),bg,Color.parseColor("#4A148C"),false));
fila.addView(celda(String.valueOf(e.coFij),bg,Color.parseColor("#2E7D32"),false));
fila.addView(celda(String.valueOf(e.coCor),bg,Color.parseColor("#E65100"),false));
fila.addView(celda(String.valueOf(e.tar),bg,Color.parseColor("#F57F17"),false));
fila.addView(celda(String.valueOf(e.noc),bg,Color.parseColor("#6A1B9A"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos");
}
void dibujarDigitosCat(List<Est> lista){
tablaStats.removeAllViews();tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] h={"Pos","Díg","Sal","Frec","Tar","Noc","%"};
TableRow enc=new TableRow(this);
for(String s:h)enc.addView(celda(s,"#1565C0",Color.WHITE,true));
tablaStats.addView(enc);
for(int i=0;i<lista.size();i++){
Est e=lista.get(i);
TableRow fila=new TableRow(this);
String bg=(i%2==0)?"#FFFFFF":"#F5F5F5";
fila.addView(celda(String.valueOf(i+1),bg,Color.parseColor("#1A237E"),true));
fila.addView(celda(String.valueOf(e.id),bg,Color.parseColor("#0D47A1"),true));
fila.addView(celda(String.valueOf(e.veces),bg,Color.parseColor("#1B5E20"),false));
fila.addView(celda(frec(e.veces),bg,Color.parseColor("#4A148C"),false));
fila.addView(celda(String.valueOf(e.tar),bg,Color.parseColor("#F57F17"),false));
fila.addView(celda(String.valueOf(e.noc),bg,Color.parseColor("#6A1B9A"),false));
fila.addView(celda(pct(e.veces),bg,Color.parseColor("#1B5E20"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos");
}
void dibujarDigitosTodos(List<Est> lista){
tablaStats.removeAllViews();tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] h={"Pos","Díg","Veces","Frec","%"};
TableRow enc=new TableRow(this);
for(String s:h)enc.addView(celda(s,"#1565C0",Color.WHITE,true));
tablaStats.addView(enc);
for(int i=0;i<lista.size();i++){
Est e=lista.get(i);
TableRow fila=new TableRow(this);
String bg=(i%2==0)?"#FFFFFF":"#F5F5F5";
fila.addView(celda(String.valueOf(i+1),bg,Color.parseColor("#1A237E"),true));
fila.addView(celda(String.valueOf(e.id),bg,Color.parseColor("#0D47A1"),true));
fila.addView(celda(String.valueOf(e.veces),bg,Color.parseColor("#1B5E20"),false));
fila.addView(celda(frec(e.veces),bg,Color.parseColor("#4A148C"),false));
fila.addView(celda(pct(e.veces),bg,Color.parseColor("#6A1B9A"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos");
}
void dibujarParlets(List<Par> lista){
tablaStats.removeAllViews();tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] h={"Pos","Parlet","Veces","Frec","Tar","Noc"};
TableRow enc=new TableRow(this);
for(String s:h)enc.addView(celda(s,"#1565C0",Color.WHITE,true));
tablaStats.addView(enc);
for(int i=0;i<lista.size();i++){
Par p=lista.get(i);
TableRow fila=new TableRow(this);
String bg=(i%2==0)?"#FFFFFF":"#F5F5F5";
fila.addView(celda(String.valueOf(i+1),bg,Color.parseColor("#1A237E"),true));
fila.addView(celda(p.par,bg,Color.parseColor("#0D47A1"),true));
fila.addView(celda(String.valueOf(p.veces),bg,Color.parseColor("#1B5E20"),false));
fila.addView(celda(frec(p.veces),bg,Color.parseColor("#4A148C"),false));
fila.addView(celda(String.valueOf(p.tar),bg,Color.parseColor("#F57F17"),false));
fila.addView(celda(String.valueOf(p.noc),bg,Color.parseColor("#6A1B9A"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos | "+lista.size()+" parlets");
}
String frec(int v){if(v==0)return "-";int f=numSorteos/v;return "1/"+f;}
String pct(int v){if(numSorteos==0)return "0%";return String.format("%.1f%%",100.0*v/numSorteos);}
TextView celda(String txt,String bg,int ct,boolean n){TextView tv=new TextView(this);tv.setText(txt);tv.setPadding(8,6,8,6);tv.setTextSize(11);tv.setTextColor(ct);tv.setBackgroundColor(Color.parseColor(bg));tv.setGravity(Gravity.CENTER);if(n)tv.setTypeface(null,Typeface.BOLD);return tv;}
int comparar(String f1,String f2){try{int[] a=parseFecha(f1);int[] b=parseFecha(f2);if(a[2]!=b[2])return a[2]-b[2];if(a[1]!=b[1])return a[1]-b[1];return a[0]-b[0];}catch(Exception e){return 0;}}
int[] parseFecha(String f){String[] p=f.split("[-/]");return new int[]{Integer.parseInt(p[0]),Integer.parseInt(p[1]),Integer.parseInt(p[2])};}
String fechaMin(){if(todos.isEmpty())return "";String m=todos.get(0).date;for(Draw d:todos){if(comparar(d.date,m)<0)m=d.date;}return m;}
String fechaMax(){if(todos.isEmpty())return "";String m=todos.get(0).date;for(Draw d:todos){if(comparar(d.date,m)>0)m=d.date;}return m;}
int parse(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
List<Draw> leerTodos(){List<Draw> l=new ArrayList<>();DB db=new DB(this);Cursor c=db.all();while(c.moveToNext())l.add(new Draw(c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6)));c.close();return l;}
static class Est{int id;int veces;int coFij;int coCor;int tar;int noc;}
static class Par{String par;int veces;int tar;int noc;Par(String p,int v,int t,int n){par=p;veces=v;tar=t;noc=n;}}
static class Draw{String date,tn,cent,f,c1,c2;Draw(String a,String b,String c,String d,String e,String f){date=a;tn=b;cent=c;this.f=d;c1=e;c2=f;}}
static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"loteria.db",null,2);}public void onCreate(SQLiteDatabase d){}public void onUpgrade(SQLiteDatabase d,int o,int n){}Cursor all(){return getReadableDatabase().query("draws",null,null,null,null,null,"id ASC");}}
}
