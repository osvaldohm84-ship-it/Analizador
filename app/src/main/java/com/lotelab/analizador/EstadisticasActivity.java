package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.net.Uri;import android.view.*;import android.widget.*;import android.graphics.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
public class EstadisticasActivity extends Activity{
EditText fechaDesde,fechaHasta;
Button btnAnalizar,btnOrdenar,btnVer,btnGuardarStats;
Button btnPagAnt,btnPagSig,btnPagPrimera,btnPagUltima;
TableLayout tablaStats;
TextView contadorStats,txtPagina;
LinearLayout barraPaginacion;
List<Draw> todos=new ArrayList<>();
int categoria=0;
int filtroPosicion=0;
int ordenActual=2;
int numSorteos=0;
int paginaActual=1;
static final int TOP_PARLETS=100;
static final int PARLETS_POR_PAGINA=200;
List<Est> ultimaLista=new ArrayList<>();
List<Par> ultimaListaPar=new ArrayList<>();
static final int CREATE_STATS=30;
static final String[] CATEGORIAS={"Números","Centenas","Decenas","Unidades","Dígitos","Parlets"};
static final String[] FILTROS_POS={"Todas","Solo Fijo","Solo C1","Solo C2","Solo Centenas","Solo Corridos"};
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_estadisticas);
fechaDesde=findViewById(R.id.fechaDesde);
fechaHasta=findViewById(R.id.fechaHasta);
btnAnalizar=findViewById(R.id.btnAnalizar);
btnOrdenar=findViewById(R.id.btnOrdenar);
btnVer=findViewById(R.id.btnVer);
btnGuardarStats=findViewById(R.id.btnGuardarStats);
tablaStats=findViewById(R.id.tablaStats);
contadorStats=findViewById(R.id.contadorStats);
btnPagAnt=findViewById(R.id.btnPagAnt);
btnPagSig=findViewById(R.id.btnPagSig);
btnPagPrimera=findViewById(R.id.btnPagPrimera);
btnPagUltima=findViewById(R.id.btnPagUltima);
txtPagina=findViewById(R.id.txtPagina);
barraPaginacion=findViewById(R.id.barraPaginacion);
new Thread(()->{
todos=leerTodos();
runOnUiThread(()->{
if(todos.size()>0){fechaDesde.setText(fechaMin());fechaHasta.setText(fechaMax());}
elegirCategoria();
});
}).start();
btnAnalizar.setOnClickListener(v->analizar());
btnOrdenar.setOnClickListener(v->elegirOrden());
btnVer.setOnClickListener(v->elegirCategoria());
btnGuardarStats.setOnClickListener(v->guardarStats());
btnPagAnt.setOnClickListener(v->cambiarPagina(-1));
btnPagSig.setOnClickListener(v->cambiarPagina(1));
btnPagPrimera.setOnClickListener(v->irAPagina(1));
btnPagUltima.setOnClickListener(v->{
if(categoria!=5)return;
int tp=(int)Math.ceil(ultimaListaPar.size()/(double)PARLETS_POR_PAGINA);
if(tp<1)tp=1;
irAPagina(tp);
});
}
@Override protected void onDestroy(){super.onDestroy();if(todos!=null)todos.clear();if(ultimaLista!=null)ultimaLista.clear();if(ultimaListaPar!=null)ultimaListaPar.clear();}
void cambiarPagina(int delta){
if(categoria!=5)return;
int totalPaginas=(int)Math.ceil(ultimaListaPar.size()/(double)PARLETS_POR_PAGINA);
if(totalPaginas<1)totalPaginas=1;
int nueva=paginaActual+delta;
if(nueva<1||nueva>totalPaginas)return;
paginaActual=nueva;
dibujarParlets();
}
void irAPagina(int pag){
if(categoria!=5)return;
int totalPaginas=(int)Math.ceil(ultimaListaPar.size()/(double)PARLETS_POR_PAGINA);
if(totalPaginas<1)totalPaginas=1;
if(pag<1||pag>totalPaginas)return;
paginaActual=pag;
dibujarParlets();
}
void elegirCategoria(){
new AlertDialog.Builder(this).setTitle("Seleccione cuál desea ver:").setCancelable(false).setItems(CATEGORIAS,(d,w)->{
categoria=w;
paginaActual=1;
if(categoria==4){elegirFiltroPosicion();}else{filtroPosicion=0;btnVer.setText("Ver: "+CATEGORIAS[categoria]);analizar();}
}).show();
}
void elegirFiltroPosicion(){
new AlertDialog.Builder(this).setTitle("Filtro posición:").setItems(FILTROS_POS,(d,w)->{
filtroPosicion=w;
btnVer.setText("Ver: Dígitos/"+FILTROS_POS[w]);
analizar();
}).show();
}
void elegirOrden(){
String[] opciones;
switch(categoria){
case 0:opciones=new String[]{"Número","Salidas","Frecuencia","Estabilidad","Como Fijo","Como Corrido","Tarde","Noche","Sin salir","Mayor sin salir","Promedio sin salir"};break;
case 1:case 2:case 3:opciones=new String[]{"Dígito","Salidas","Frecuencia","Estabilidad","Tarde","Noche","Porcentaje","Sin salir","Mayor sin salir","Promedio sin salir"};break;
case 4:opciones=new String[]{"Dígito","Apariciones","Frecuencia","Estabilidad","Porcentaje","Sin salir","Mayor sin salir","Promedio sin salir"};break;
case 5:opciones=new String[]{"Parlet","Apariciones","Frecuencia","Estabilidad","Tarde","Noche"};break;
default:opciones=new String[]{"Pos"};break;
}
new AlertDialog.Builder(this).setTitle("Ordenar por:").setItems(opciones,(d,w)->{ordenActual=w;paginaActual=1;analizar();}).show();
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
contadorStats.setText("Calculando...");
barraPaginacion.setVisibility(View.GONE);
new Thread(()->{
try{
switch(categoria){
case 0:calcNumeros(filtrados);break;
case 1:calcDigCat(filtrados,1);break;
case 2:calcDigCat(filtrados,2);break;
case 3:calcDigCat(filtrados,3);break;
case 4:calcDigTodos(filtrados);break;
case 5:calcParlets(filtrados);break;
}
}catch(final Exception ex){
runOnUiThread(()->{
contadorStats.setText("Error: "+ex.getClass().getSimpleName());
Toast.makeText(this,"Error: "+ex.getMessage(),Toast.LENGTH_LONG).show();
});
}
}).start();
}
void calcNumeros(List<Draw> filt){
List<Integer>[] posiciones=new List[100];
for(int i=0;i<100;i++)posiciones[i]=new ArrayList<>();
Est[] ests=new Est[100];
for(int i=0;i<100;i++){ests[i]=new Est();ests[i].id=i;}
int pos=0;
for(Draw d:filt){
int[] vals={parse(d.f),parse(d.c1),parse(d.c2)};
for(int k=0;k<3;k++){int x=vals[k];if(x>=0&&x<100){ests[x].veces++;if(k==0)ests[x].coFij++;else ests[x].coCor++;if(d.tn.equals("T"))ests[x].tar++;else ests[x].noc++;posiciones[x].add(pos);}}
pos++;
}
for(int i=0;i<100;i++){calcularSS(ests[i],posiciones[i],filt.size());ests[i].estab=calcularEstab(posiciones[i]);}
List<Est> lista=new ArrayList<>(Arrays.asList(ests));
ordenarEst(lista);
ultimaLista=lista;
runOnUiThread(()->dibujarNumeros(lista));
}
void calcDigCat(List<Draw> filt,int posCat){
int[] cnt=new int[10];int[] tar=new int[10];int[] noc=new int[10];
List<Integer>[] posiciones=new List[10];
for(int i=0;i<10;i++)posiciones[i]=new ArrayList<>();
int p=0;
for(Draw d:filt){
int dig=-1;
switch(posCat){
case 1:dig=parse(d.cent);break;
case 2:int fij=parse(d.f);if(fij>=0)dig=fij/10;break;
case 3:int fij2=parse(d.f);if(fij2>=0)dig=fij2%10;break;
}
if(dig<0||dig>9)continue;
cnt[dig]++;posiciones[dig].add(p);
if(d.tn.equals("T"))tar[dig]++;else noc[dig]++;
p++;
}
List<Est> lista=new ArrayList<>();
for(int i=0;i<10;i++){Est e=new Est();e.id=i;e.veces=cnt[i];e.tar=tar[i];e.noc=noc[i];calcularSS(e,posiciones[i],filt.size());e.estab=calcularEstab(posiciones[i]);lista.add(e);}
ordenarEst(lista);
ultimaLista=lista;
runOnUiThread(()->dibujarDigCat(lista));
}
void calcDigTodos(List<Draw> filt){
int[] cnt=new int[10];
List<Integer>[] posiciones=new List[10];
for(int i=0;i<10;i++)posiciones[i]=new ArrayList<>();
int p=0;
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
for(char c:v.toCharArray()){if(Character.isDigit(c)){int dig=c-'0';cnt[dig]++;posiciones[dig].add(p);}}
}
p++;
}
List<Est> lista=new ArrayList<>();
for(int i=0;i<10;i++){Est e=new Est();e.id=i;e.veces=cnt[i];calcularSS(e,posiciones[i],filt.size());e.estab=calcularEstab(posiciones[i]);lista.add(e);}
ordenarEst(lista);
ultimaLista=lista;
runOnUiThread(()->dibujarDigTodos(lista));
}
void calcParlets(List<Draw> filt){
Map<String,int[]> mapa=new HashMap<>();
int p=0;
for(Draw d:filt){
int f=parse(d.f);int c1=parse(d.c1);int c2=parse(d.c2);
if(f<0||c1<0||c2<0)continue;
String[] pares={par(f,c1),par(f,c2),par(c1,c2)};
for(String pp:pares){
if(!mapa.containsKey(pp))mapa.put(pp,new int[3]);
mapa.get(pp)[0]++;
if(d.tn.equals("T"))mapa.get(pp)[1]++;else mapa.get(pp)[2]++;
}
p++;
}
List<Map.Entry<String,int[]>> entries=new ArrayList<>(mapa.entrySet());
Collections.sort(entries,(a,b)->b.getValue()[0]-a.getValue()[0]);
int limite=Math.min(TOP_PARLETS,entries.size());
Map<String,List<Integer>> posicTop=new HashMap<>();
for(int i=0;i<limite;i++){
String pp=entries.get(i).getKey();
posicTop.put(pp,new ArrayList<>());
}
int pp2=0;
for(Draw d:filt){
int f=parse(d.f);int c1=parse(d.c1);int c2=parse(d.c2);
if(f<0||c1<0||c2<0)continue;
String[] pares={par(f,c1),par(f,c2),par(c1,c2)};
for(String pp:pares){
List<Integer> lst=posicTop.get(pp);
if(lst!=null)lst.add(pp2);
}
pp2++;
}
List<Par> lista=new ArrayList<>();
for(int i=0;i<limite;i++){
Map.Entry<String,int[]> e=entries.get(i);
Par par=new Par(e.getKey(),e.getValue()[0],e.getValue()[1],e.getValue()[2]);
par.estab=calcularEstab(posicTop.get(e.getKey()));
lista.add(par);
}
for(int i=limite;i<entries.size();i++){
Map.Entry<String,int[]> e=entries.get(i);
Par par=new Par(e.getKey(),e.getValue()[0],e.getValue()[1],e.getValue()[2]);
par.estab=-1;
lista.add(par);
}
posicTop.clear();
ordenarPar(lista);
ultimaListaPar=lista;
runOnUiThread(()->dibujarParlets());
}
void calcularSS(Est e,List<Integer> posiciones,int totalSorteos){
if(posiciones==null||posiciones.isEmpty()){e.siSal=-1;e.maySS=-1;e.proSS=-1;return;}
e.siSal=totalSorteos-posiciones.get(posiciones.size()-1)-1;
int may=0;long suma=0;
int prev=0;
for(int i=0;i<posiciones.size();i++){
int actual=posiciones.get(i);
int gap=actual-prev;
if(gap>may)may=gap;
suma+=gap;
prev=actual;
}
int ultimoGap=totalSorteos-prev;
if(ultimoGap>may)may=ultimoGap;
suma+=ultimoGap;
e.maySS=may;
e.proSS=(int)(suma/posiciones.size());
}
void ordenarPar(List<Par> lista){
switch(ordenActual){
case 0:Collections.sort(lista,(a,b)->a.par.compareTo(b.par));break;
case 1:Collections.sort(lista,(a,b)->b.veces-a.veces);break;
case 2:Collections.sort(lista,(a,b)->frecComp(b.veces,a.veces));break;
case 3:Collections.sort(lista,(a,b)->Double.compare(a.estab<0?99999:a.estab,b.estab<0?99999:b.estab));break;
case 4:Collections.sort(lista,(a,b)->b.tar-a.tar);break;
case 5:Collections.sort(lista,(a,b)->b.noc-a.noc);break;
}
}
double calcularEstab(List<Integer> posiciones){
if(posiciones==null||posiciones.size()<3)return -1;
List<Integer> intervalos=new ArrayList<>();
for(int i=1;i<posiciones.size();i++)intervalos.add(posiciones.get(i)-posiciones.get(i-1));
double suma=0;for(int x:intervalos)suma+=x;
double prom=suma/intervalos.size();
if(prom==0)return -1;
double varianza=0;for(int x:intervalos)varianza+=(x-prom)*(x-prom);
varianza/=intervalos.size();
double desv=Math.sqrt(varianza);
double res=(desv/prom)*100;
if(Double.isNaN(res)||Double.isInfinite(res))return -1;
return res;
}
String par(int a,int b){return a<=b?String.format("%02d-%02d",a,b):String.format("%02d-%02d",b,a);}
void ordenarEst(List<Est> lista){
switch(ordenActual){
case 0:Collections.sort(lista,(a,b)->a.id-b.id);break;
case 1:Collections.sort(lista,(a,b)->b.veces-a.veces);break;
case 2:Collections.sort(lista,(a,b)->frecComp(b.veces,a.veces));break;
case 3:Collections.sort(lista,(a,b)->Double.compare(a.estab<0?99999:a.estab,b.estab<0?99999:b.estab));break;
case 4:Collections.sort(lista,(a,b)->b.coFij-a.coFij);break;
case 5:Collections.sort(lista,(a,b)->b.coCor-a.coCor);break;
case 6:Collections.sort(lista,(a,b)->b.tar-a.tar);break;
case 7:Collections.sort(lista,(a,b)->b.noc-a.noc);break;
case 8:Collections.sort(lista,(a,b)->(a.siSal<0?99999:a.siSal)-(b.siSal<0?99999:b.siSal));break;
case 9:Collections.sort(lista,(a,b)->(b.maySS<0?-1:b.maySS)-(a.maySS<0?-1:a.maySS));break;
case 10:Collections.sort(lista,(a,b)->(a.proSS<0?99999:a.proSS)-(b.proSS<0?99999:b.proSS));break;
}
}
int frecComp(int a,int b){if(a==0&&b==0)return 0;if(a==0)return 1;if(b==0)return -1;return b-a;}
int colorEstab(double e){if(e<0)return Color.parseColor("#9E9E9E");if(e<20)return Color.parseColor("#1B5E20");if(e<50)return Color.parseColor("#F57F17");return Color.parseColor("#1565C0");}
String estabTxt(double e){if(e<0)return "N/A";return String.format("%.1f",e);}
String ssTxt(int v){if(v<0)return "-";return String.valueOf(v);}
void dibujarNumeros(List<Est> lista){
barraPaginacion.setVisibility(View.GONE);
tablaStats.removeAllViews();tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] h={"Pos","#","Sal","Frec","Estab","Fij","Cor","Tar","Noc","SiSal","MaySS","ProSS"};
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
TextView tvEst=new TextView(this);tvEst.setText(estabTxt(e.estab));tvEst.setPadding(8,6,8,6);tvEst.setTextSize(11);tvEst.setTextColor(colorEstab(e.estab));tvEst.setBackgroundColor(Color.parseColor(bg));tvEst.setGravity(Gravity.CENTER);tvEst.setTypeface(null,Typeface.BOLD);fila.addView(tvEst);
fila.addView(celda(String.valueOf(e.coFij),bg,Color.parseColor("#2E7D32"),false));
fila.addView(celda(String.valueOf(e.coCor),bg,Color.parseColor("#E65100"),false));
fila.addView(celda(String.valueOf(e.tar),bg,Color.parseColor("#F57F17"),false));
fila.addView(celda(String.valueOf(e.noc),bg,Color.parseColor("#6A1B9A"),false));
fila.addView(celda(ssTxt(e.siSal),bg,Color.parseColor("#C62828"),false));
fila.addView(celda(ssTxt(e.maySS),bg,Color.parseColor("#D84315"),false));
fila.addView(celda(ssTxt(e.proSS),bg,Color.parseColor("#AD1457"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos");
}
void dibujarDigCat(List<Est> lista){
barraPaginacion.setVisibility(View.GONE);
tablaStats.removeAllViews();tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] h={"Pos","Díg","Sal","Frec","Estab","Tar","Noc","%","SiSal","MaySS","ProSS"};
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
TextView tvEst=new TextView(this);tvEst.setText(estabTxt(e.estab));tvEst.setPadding(8,6,8,6);tvEst.setTextSize(11);tvEst.setTextColor(colorEstab(e.estab));tvEst.setBackgroundColor(Color.parseColor(bg));tvEst.setGravity(Gravity.CENTER);tvEst.setTypeface(null,Typeface.BOLD);fila.addView(tvEst);
fila.addView(celda(String.valueOf(e.tar),bg,Color.parseColor("#F57F17"),false));
fila.addView(celda(String.valueOf(e.noc),bg,Color.parseColor("#6A1B9A"),false));
fila.addView(celda(pct(e.veces),bg,Color.parseColor("#1B5E20"),false));
fila.addView(celda(ssTxt(e.siSal),bg,Color.parseColor("#C62828"),false));
fila.addView(celda(ssTxt(e.maySS),bg,Color.parseColor("#D84315"),false));
fila.addView(celda(ssTxt(e.proSS),bg,Color.parseColor("#AD1457"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos");
}
void dibujarDigTodos(List<Est> lista){
barraPaginacion.setVisibility(View.GONE);
tablaStats.removeAllViews();tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] h={"Pos","Díg","Veces","Frec","Estab","%","SiSal","MaySS","ProSS"};
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
TextView tvEst=new TextView(this);tvEst.setText(estabTxt(e.estab));tvEst.setPadding(8,6,8,6);tvEst.setTextSize(11);tvEst.setTextColor(colorEstab(e.estab));tvEst.setBackgroundColor(Color.parseColor(bg));tvEst.setGravity(Gravity.CENTER);tvEst.setTypeface(null,Typeface.BOLD);fila.addView(tvEst);
fila.addView(celda(pct(e.veces),bg,Color.parseColor("#6A1B9A"),false));
fila.addView(celda(ssTxt(e.siSal),bg,Color.parseColor("#C62828"),false));
fila.addView(celda(ssTxt(e.maySS),bg,Color.parseColor("#D84315"),false));
fila.addView(celda(ssTxt(e.proSS),bg,Color.parseColor("#AD1457"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos");
}
void dibujarParlets(){
List<Par> lista=ultimaListaPar;
int total=lista.size();
int totalPaginas=(int)Math.ceil(total/(double)PARLETS_POR_PAGINA);
if(totalPaginas<1)totalPaginas=1;
if(paginaActual>totalPaginas)paginaActual=totalPaginas;
if(paginaActual<1)paginaActual=1;
int inicio=(paginaActual-1)*PARLETS_POR_PAGINA;
int fin=Math.min(inicio+PARLETS_POR_PAGINA,total);
barraPaginacion.setVisibility(totalPaginas>1?View.VISIBLE:View.GONE);
txtPagina.setText("Pág: "+paginaActual+"/"+totalPaginas);
btnPagPrimera.setEnabled(paginaActual>1);
btnPagAnt.setEnabled(paginaActual>1);
btnPagSig.setEnabled(paginaActual<totalPaginas);
btnPagUltima.setEnabled(paginaActual<totalPaginas);
tablaStats.removeAllViews();tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] h={"Pos","Parlet","Veces","Frec","Estab","Tar","Noc"};
TableRow enc=new TableRow(this);
for(String s:h)enc.addView(celda(s,"#1565C0",Color.WHITE,true));
tablaStats.addView(enc);
for(int i=inicio;i<fin;i++){
Par p=lista.get(i);
TableRow fila=new TableRow(this);
String bg=((i-inicio)%2==0)?"#FFFFFF":"#F5F5F5";
fila.addView(celda(String.valueOf(i+1),bg,Color.parseColor("#1A237E"),true));
fila.addView(celda(p.par,bg,Color.parseColor("#0D47A1"),true));
fila.addView(celda(String.valueOf(p.veces),bg,Color.parseColor("#1B5E20"),false));
fila.addView(celda(frec(p.veces),bg,Color.parseColor("#4A148C"),false));
TextView tvEst=new TextView(this);tvEst.setText(estabTxt(p.estab));tvEst.setPadding(8,6,8,6);tvEst.setTextSize(11);tvEst.setTextColor(colorEstab(p.estab));tvEst.setBackgroundColor(Color.parseColor(bg));tvEst.setGravity(Gravity.CENTER);tvEst.setTypeface(null,Typeface.BOLD);fila.addView(tvEst);
fila.addView(celda(String.valueOf(p.tar),bg,Color.parseColor("#F57F17"),false));
fila.addView(celda(String.valueOf(p.noc),bg,Color.parseColor("#6A1B9A"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos | "+total+" parlets");
}
void guardarStats(){
if((categoria==5&&ultimaListaPar.isEmpty())||(categoria!=5&&ultimaLista.isEmpty())){Toast.makeText(this,"Primero analice",Toast.LENGTH_SHORT).show();return;}
Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("text/csv");intent.putExtra(Intent.EXTRA_TITLE,"estadisticas.csv");startActivityForResult(intent,CREATE_STATS);
}
@Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r!=CREATE_STATS||c!=RESULT_OK||d==null)return;
Uri u=d.getData();
new Thread(()->{
try{
OutputStream out=getContentResolver().openOutputStream(u);
Writer w=new OutputStreamWriter(out,StandardCharsets.UTF_8);
w.write("Categoria,"+CATEGORIAS[categoria]+"\n");
w.write("Sorteos,"+numSorteos+"\n");
w.write("Desde,"+fechaDesde.getText()+"\n");
w.write("Hasta,"+fechaHasta.getText()+"\n\n");
if(categoria==5){
w.write("Pos,Parlet,Veces,Frecuencia,Estab,Tarde,Noche\n");
for(int i=0;i<ultimaListaPar.size();i++){Par p=ultimaListaPar.get(i);w.write((i+1)+","+p.par+","+p.veces+","+frec(p.veces)+","+estabTxt(p.estab)+","+p.tar+","+p.noc+"\n");}
}else{
w.write("Pos,Valor,Salidas,Frecuencia,Estab,SiSal,MaySS,ProSS\n");
for(int i=0;i<ultimaLista.size();i++){Est e=ultimaLista.get(i);w.write((i+1)+","+e.id+","+e.veces+","+frec(e.veces)+","+estabTxt(e.estab)+","+ssTxt(e.siSal)+","+ssTxt(e.maySS)+","+ssTxt(e.proSS)+"\n");}
}
w.close();
runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Listo").setMessage("Estadisticas guardadas").setPositiveButton("OK",null).show());
}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());}
}).start();
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
static class Est{int id;int veces;int coFij;int coCor;int tar;int noc;int siSal=-1;int maySS=-1;int proSS=-1;double estab=-1;}
static class Par{String par;int veces;int tar;int noc;double estab=-1;Par(String p,int v,int t,int n){par=p;veces=v;tar
