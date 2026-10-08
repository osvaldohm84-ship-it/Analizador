package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.view.*;import android.widget.*;import android.graphics.*;import java.util.*;
public class EstadisticasActivity extends Activity{
EditText fechaDesde,fechaHasta;
Button btnAnalizar,btnOrdenar;
TableLayout tablaStats;
TextView contadorStats;
List<Draw> todos=new ArrayList<>();
List<Est> resultados=new ArrayList<>();
int ordenActual=2;
static final String[] NOMBRES_ORDEN={"Pos","Numero","Veces Salido","Como Fijo","Como Corrido","Turno Tarde","Turno Noche"};
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_estadisticas);
fechaDesde=findViewById(R.id.fechaDesde);
fechaHasta=findViewById(R.id.fechaHasta);
btnAnalizar=findViewById(R.id.btnAnalizar);
btnOrdenar=findViewById(R.id.btnOrdenar);
tablaStats=findViewById(R.id.tablaStats);
contadorStats=findViewById(R.id.contadorStats);
new Thread(()->{
todos=leerTodos();
runOnUiThread(()->{
if(todos.size()>0){
fechaDesde.setText(fechaMin());
fechaHasta.setText(fechaMax());
}
});
}).start();
btnAnalizar.setOnClickListener(v->analizar());
btnOrdenar.setOnClickListener(v->elegirOrden());
}
void elegirOrden(){
new AlertDialog.Builder(this).setTitle("Ordenar por:").setItems(NOMBRES_ORDEN,(d,w)->{
ordenActual=w;
analizar();
}).show();
}
void analizar(){
String desde=fechaDesde.getText().toString().trim();
String hasta=fechaHasta.getText().toString().trim();
if(todos.isEmpty()){Toast.makeText(this,"Cargando datos...",Toast.LENGTH_SHORT).show();return;}
List<Draw> filtrados=new ArrayList<>();
for(Draw d:todos){
if(!desde.isEmpty()&&comparar(d.date,desde)<0)continue;
if(!hasta.isEmpty()&&comparar(d.date,hasta)>0)continue;
filtrados.add(d);
}
if(filtrados.isEmpty()){Toast.makeText(this,"No hay sorteos en ese rango",Toast.LENGTH_SHORT).show();tablaStats.removeAllViews();contadorStats.setText("0 sorteos");return;}
final int numSorteos=filtrados.size();
new Thread(()->{
Est[] ests=new Est[100];
for(int i=0;i<100;i++){ests[i]=new Est();ests[i].num=i;}
for(Draw d:filtrados){
int[] vals={parse(d.f),parse(d.c1),parse(d.c2)};
for(int k=0;k<3;k++){
int x=vals[k];
if(x>=0&&x<100){
ests[x].salido++;
if(k==0)ests[x].coFij++;
else ests[x].coCor++;
if(d.tn.equals("T"))ests[x].poTar++;
else ests[x].poNoc++;
}
}
}
List<Est> lista=new ArrayList<>();
for(Est e:ests)lista.add(e);
switch(ordenActual){
case 0:break;
case 1:Collections.sort(lista,(a,b)->a.num-b.num);break;
case 2:Collections.sort(lista,(a,b)->b.salido-a.salido);break;
case 3:Collections.sort(lista,(a,b)->b.coFij-a.coFij);break;
case 4:Collections.sort(lista,(a,b)->b.coCor-a.coCor);break;
case 5:Collections.sort(lista,(a,b)->b.poTar-a.poTar);break;
case 6:Collections.sort(lista,(a,b)->b.poNoc-a.poNoc);break;
}
for(int i=0;i<lista.size();i++)lista.get(i).pos=i+1;
runOnUiThread(()->{dibujarTabla(lista,numSorteos);});
}).start();
}
void dibujarTabla(List<Est> lista,int numSorteos){
tablaStats.removeAllViews();
tablaStats.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] headers={"Pos","#","Sal","Fij","Cor","Tar","Noc"};
TableRow enc=new TableRow(this);
for(String h:headers){enc.addView(celda(h,"#1565C0",Color.WHITE,true));}
tablaStats.addView(enc);
for(int i=0;i<lista.size();i++){
Est e=lista.get(i);
TableRow fila=new TableRow(this);
String bg=(i%2==0)?"#FFFFFF":"#F5F5F5";
fila.addView(celda(String.valueOf(e.pos),bg,Color.parseColor("#1A237E"),true));
fila.addView(celda(String.format("%02d",e.num),bg,Color.parseColor("#0D47A1"),true));
fila.addView(celda(String.valueOf(e.salido),bg,Color.parseColor("#1B5E20"),false));
fila.addView(celda(String.valueOf(e.coFij),bg,Color.parseColor("#2E7D32"),false));
fila.addView(celda(String.valueOf(e.coCor),bg,Color.parseColor("#E65100"),false));
fila.addView(celda(String.valueOf(e.poTar),bg,Color.parseColor("#F57F17"),false));
fila.addView(celda(String.valueOf(e.poNoc),bg,Color.parseColor("#4A148C"),false));
tablaStats.addView(fila);
}
contadorStats.setText(numSorteos+" sorteos | Orden: "+NOMBRES_ORDEN[ordenActual]);
}
TextView celda(String txt,String bg,int colorTexto,boolean negrita){
TextView tv=new TextView(this);
tv.setText(txt);
tv.setPadding(10,8,10,8);
tv.setTextSize(12);
tv.setTextColor(colorTexto);
tv.setBackgroundColor(Color.parseColor(bg));
tv.setGravity(Gravity.CENTER);
if(negrita)tv.setTypeface(null,Typeface.BOLD);
return tv;
}
int comparar(String f1,String f2){
try{
int[] a=parseFecha(f1);
int[] b=parseFecha(f2);
if(a[2]!=b[2])return a[2]-b[2];
if(a[1]!=b[1])return a[1]-b[1];
return a[0]-b[0];
}catch(Exception e){return 0;}
}
int[] parseFecha(String f){
String[] p=f.split("[-/]");
return new int[]{Integer.parseInt(p[0]),Integer.parseInt(p[1]),Integer.parseInt(p[2])};
}
String fechaMin(){
if(todos.isEmpty())return "";
String min=todos.get(0).date;
for(Draw d:todos){if(comparar(d.date,min)<0)min=d.date;}
return min;
}
String fechaMax(){
if(todos.isEmpty())return "";
String max=todos.get(0).date;
for(Draw d:todos){if(comparar(d.date,max)>0)max=d.date;}
return max;
}
int parse(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
List<Draw> leerTodos(){
List<Draw> lista=new ArrayList<>();
DB db=new DB(this);
Cursor c=db.all();
while(c.moveToNext())lista.add(new Draw(c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6)));
c.close();
return lista;
}
static class Est{int num;int salido;int coFij;int coCor;int poTar;int poNoc;int pos;}
static class Draw{String date,tn,cent,f,c1,c2;Draw(String a,String b,String c,String d,String e,String f){date=a;tn=b;cent=c;this.f=d;c1=e;c2=f;}}
static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"loteria.db",null,2);}public void onCreate(SQLiteDatabase d){}public void onUpgrade(SQLiteDatabase d,int o,int n){}Cursor all(){return getReadableDatabase().query("draws",null,null,null,null,null,"id ASC");}}
}
