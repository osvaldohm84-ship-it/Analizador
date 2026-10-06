package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.view.*;import android.widget.*;import android.graphics.*;import java.util.*;
public class BuscarNumerosActivity extends Activity{
LinearLayout grillaNumeros,grillaDecenas,grillaTerminales;
TextView seleccionados;
Button btnBuscar;
Set<Integer> numSel=new HashSet<>();
Set<Integer> decSel=new HashSet<>();
Set<Integer> termSel=new HashSet<>();
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_buscar_numeros);
grillaNumeros=findViewById(R.id.grillaNumeros);
grillaDecenas=findViewById(R.id.grillaDecenas);
grillaTerminales=findViewById(R.id.grillaTerminales);
seleccionados=findViewById(R.id.seleccionados);
btnBuscar=findViewById(R.id.btnBuscar);
crearGrillaNumeros();
crearGrillaDecTerm(grillaDecenas,true);
crearGrillaDecTerm(grillaTerminales,false);
btnBuscar.setOnClickListener(v->buscar());
}
void crearGrillaNumeros(){
grillaNumeros.removeAllViews();
for(int fila=0;fila<10;fila++){
LinearLayout fl=new LinearLayout(this);
fl.setOrientation(LinearLayout.HORIZONTAL);
fl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
for(int col=0;col<10;col++){
int num=fila*10+col;
Button btn=new Button(this);
btn.setText(String.format("%02d",num));
btn.setTextSize(11);
btn.setPadding(0,0,0,0);
final int n=num;
btn.setOnClickListener(v->{if(numSel.contains(n))numSel.remove(n);else numSel.add(n);actualizarGrilla();});
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
lp.setMargins(1,1,1,1);
btn.setLayoutParams(lp);
aplicarColor(btn,numSel.contains(num));
fl.addView(btn);
}
grillaNumeros.addView(fl);
}
}
void crearGrillaDecTerm(LinearLayout contenedor,final boolean esDecena){
contenedor.removeAllViews();
for(int i=0;i<10;i++){
final int d=i;
Button btn=new Button(this);
btn.setText(String.valueOf(i));
btn.setTextSize(14);
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
lp.setMargins(2,2,2,2);
btn.setLayoutParams(lp);
btn.setOnClickListener(v->{
if(esDecena){if(decSel.contains(d))decSel.remove(d);else decSel.add(d);}
else{if(termSel.contains(d))termSel.remove(d);else termSel.add(d);}
actualizarGrilla();
});
boolean activo=esDecena?decSel.contains(i):termSel.contains(i);
aplicarColor(btn,activo);
contenedor.addView(btn);
}
}
void aplicarColor(Button b,boolean activo){
if(activo){b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8BC34A")));b.setTextColor(Color.WHITE);}
else{b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));b.setTextColor(Color.parseColor("#00897B"));}
}
void actualizarGrilla(){
crearGrillaNumeros();
crearGrillaDecTerm(grillaDecenas,true);
crearGrillaDecTerm(grillaTerminales,false);
StringBuilder sb=new StringBuilder("Seleccionados: ");
boolean algo=false;
if(!numSel.isEmpty()){List<Integer> l=new ArrayList<>(numSel);Collections.sort(l);sb.append("N=");for(int i=0;i<l.size();i++){sb.append(String.format("%02d",l.get(i)));if(i<l.size()-1)sb.append(",");}algo=true;}
if(!decSel.isEmpty()){if(algo)sb.append(" | ");List<Integer> l=new ArrayList<>(decSel);Collections.sort(l);sb.append("D=");for(int i=0;i<l.size();i++){sb.append(l.get(i));if(i<l.size()-1)sb.append(",");}algo=true;}
if(!termSel.isEmpty()){if(algo)sb.append(" | ");List<Integer> l=new ArrayList<>(termSel);Collections.sort(l);sb.append("T=");for(int i=0;i<l.size();i++){sb.append(l.get(i));if(i<l.size()-1)sb.append(",");}algo=true;}
if(!algo)sb.append("(ninguno)");
seleccionados.setText(sb.toString());
}
void buscar(){
if(numSel.isEmpty()&&decSel.isEmpty()&&termSel.isEmpty()){Toast.makeText(this,"Seleccione al menos un valor",Toast.LENGTH_SHORT).show();return;}
new Thread(()->{
List<Draw> todos=leerTodos();
StringBuilder res=new StringBuilder();
if(!numSel.isEmpty()){
List<Integer> l=new ArrayList<>(numSel);Collections.sort(l);
res.append("=== NUMEROS ===\n");
for(int n:l){
int[][] cntF=new int[32][1];
int[][] cntC1=new int[32][1];
int[][] cntC2=new int[32][1];
for(Draw d:todos){
int dia=extraerDia(d.date);if(dia<0||dia>31)continue;
int f=parse(d.f),c1=parse(d.c1),c2=parse(d.c2);
if(f==n)cntF[dia][0]++;
if(c1==n)cntC1[dia][0]++;
if(c2==n)cntC2[dia][0]++;
}
res.append("Numero ").append(String.format("%02d",n)).append(":\n");
res.append("  FIJO: ").append(top10dias(cntF)).append("\n");
res.append("  C1: ").append(top10dias(cntC1)).append("\n");
res.append("  C2: ").append(top10dias(cntC2)).append("\n");
}
}
if(!decSel.isEmpty()){
res.append("\n=== DECENAS ===\n");
for(int d:decSel){
int[] cnt=new int[32];
int[] cntC1=new int[32];
int[] cntC2=new int[32];
for(Draw dr:todos){
int dia=extraerDia(dr.date);if(dia<0||dia>31)continue;
int f=parse(dr.f);if(f>=0&&f/10==d)cnt[dia]++;
int c1=parse(dr.c1);if(c1>=0&&c1/10==d)cntC1[dia]++;
int c2=parse(dr.c2);if(c2>=0&&c2/10==d)cntC2[dia]++;
}
res.append("Decena ").append(d).append(":\n");
res.append("  FIJO: ").append(top10diasInt(cnt)).append("\n");
res.append("  C1: ").append(top10diasInt(cntC1)).append("\n");
res.append("  C2: ").append(top10diasInt(cntC2)).append("\n");
}
}
if(!termSel.isEmpty()){
res.append("\n=== TERMINALES ===\n");
for(int t:termSel){
int[] cnt=new int[32];
int[] cntC1=new int[32];
int[] cntC2=new int[32];
for(Draw dr:todos){
int dia=extraerDia(dr.date);if(dia<0||dia>31)continue;
int f=parse(dr.f);if(f>=0&&f%10==t)cnt[dia]++;
int c1=parse(dr.c1);if(c1>=0&&c1%10==t)cntC1[dia]++;
int c2=parse(dr.c2);if(c2>=0&&c2%10==t)cntC2[dia]++;
}
res.append("Terminal ").append(t).append(":\n");
res.append("  FIJO: ").append(top10diasInt(cnt)).append("\n");
res.append("  C1: ").append(top10diasInt(cntC1)).append("\n");
res.append("  C2: ").append(top10diasInt(cntC2)).append("\n");
}
}
final String r=res.toString();
runOnUiThread(()->mostrarResultado(r));
}).start();
}
int extraerDia(String fecha){try{String[] p=fecha.split("[-/]");return Integer.parseInt(p[0]);}catch(Exception e){return -1;}}
int parse(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
String top10dias(int[][] arr){
int n=32;int[] ix=new int[n];for(int i=0;i<n;i++)ix[i]=i;
for(int i=0;i<n;i++)for(int j=i+1;j<n;j++)if(arr[ix[j]][0]>arr[ix[i]][0]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}
StringBuilder sb=new StringBuilder();
for(int i=0;i<10&&i<n;i++){if(arr[ix[i]][0]==0)break;sb.append(ix[i]).append("(").append(arr[ix[i]][0]).append(") ");}
return sb.length()==0?"(sin datos)":sb.toString();
}
String top10diasInt(int[] arr){
int n=32;int[] ix=new int[n];for(int i=0;i<n;i++)ix[i]=i;
for(int i=0;i<n;i++)for(int j=i+1;j<n;j++)if(arr[ix[j]]>arr[ix[i]]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}
StringBuilder sb=new StringBuilder();
for(int i=0;i<10&&i<n;i++){if(arr[ix[i]]==0)break;sb.append(ix[i]).append("(").append(arr[ix[i]]).append(") ");}
return sb.length()==0?"(sin datos)":sb.toString();
}
List<Draw> leerTodos(){
List<Draw> lista=new ArrayList<>();
DB db=new DB(this);
Cursor c=db.all();
while(c.moveToNext())lista.add(new Draw(c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6)));
c.close();
return lista;
}
void mostrarResultado(String cuerpo){
AlertDialog.Builder b=new AlertDialog.Builder(this);
b.setTitle("Resultado");
ScrollView sv=new ScrollView(this);
TextView tv=new TextView(this);
tv.setText(cuerpo);
tv.setPadding(30,30,30,30);
tv.setTextSize(13);
sv.addView(tv);
b.setView(sv);
b.setPositiveButton("Cerrar",null);
b.show();
}
static class Draw{String date,tn,cent,f,c1,c2;Draw(String a,String b,String c,String d,String e,String f){date=a;tn=b;cent=c;this.f=d;c1=e;c2=f;}}
static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"loteria.db",null,1);}public void onCreate(SQLiteDatabase d){}public void onUpgrade(SQLiteDatabase d,int o,int n){}Cursor all(){return getReadableDatabase().query("draws",null,null,null,null,null,"id ASC");}}
}
