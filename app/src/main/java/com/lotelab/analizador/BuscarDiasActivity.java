package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.view.*;import android.widget.*;import android.graphics.*;import android.graphics.drawable.*;import java.util.*;
public class BuscarDiasActivity extends Activity{
LinearLayout lineaRangos1,lineaRangos2,grillaDias;
TextView diasSeleccionados;
Button btnBuscar;
Set<Integer> seleccionados=new HashSet<>();
static final int[] RANGO_INICIO={1,6,11,16,21,26};
static final int[] RANGO_FIN={5,10,15,20,25,31};
static final String[] RANGO_NOMBRE={"1-5","6-10","11-15","16-20","21-25","26-31"};
@Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_buscar_dias);
lineaRangos1=findViewById(R.id.lineaRangos1);lineaRangos2=findViewById(R.id.lineaRangos2);grillaDias=findViewById(R.id.grillaDias);diasSeleccionados=findViewById(R.id.diasSeleccionados);btnBuscar=findViewById(R.id.btnBuscar);
crearBotonesRangos();
crearGrillaDias();
btnBuscar.setOnClickListener(v->buscar());
}
void crearBotonesRangos(){
for(int i=0;i<6;i++){
Button btn=new Button(this);
btn.setText(RANGO_NOMBRE[i]);
final int idx=i;
btn.setOnClickListener(v->{
boolean yaEstan=true;
for(int d=RANGO_INICIO[idx];d<=RANGO_FIN[idx];d++){if(!seleccionados.contains(d)){yaEstan=false;break;}}
if(yaEstan){for(int d=RANGO_INICIO[idx];d<=RANGO_FIN[idx];d++)seleccionados.remove(d);}
else{for(int d=RANGO_INICIO[idx];d<=RANGO_FIN[idx];d++)seleccionados.add(d);}
actualizarGrilla();
});
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
lp.setMargins(2,2,2,2);
btn.setLayoutParams(lp);
btn.setTextSize(12);
if(i<3)lineaRangos1.addView(btn);else lineaRangos2.addView(btn);
}
}
void crearGrillaDias(){
grillaDias.removeAllViews();
for(int fila=0;fila<5;fila++){
LinearLayout filaLayout=new LinearLayout(this);
filaLayout.setOrientation(LinearLayout.HORIZONTAL);
filaLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
for(int col=0;col<7;col++){
int dia=fila*7+col+1;
if(dia>31)break;
Button btnDia=new Button(this);
btnDia.setText(String.valueOf(dia));
btnDia.setTextSize(14);
btnDia.setTag(dia);
final int d=dia;
btnDia.setOnClickListener(v->{if(seleccionados.contains(d))seleccionados.remove(d);else seleccionados.add(d);actualizarGrilla();});
LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
lp.setMargins(3,3,3,3);
btnDia.setLayoutParams(lp);
if(seleccionados.contains(dia)){btnDia.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#8BC34A")));btnDia.setTextColor(Color.WHITE);}
else{btnDia.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));btnDia.setTextColor(Color.parseColor("#00897B"));}
filaLayout.addView(btnDia);
}
grillaDias.addView(filaLayout);
}
}
void actualizarGrilla(){
crearGrillaDias();
if(seleccionados.isEmpty()){diasSeleccionados.setText("Días seleccionados: (ninguno)");return;}
List<Integer> lista=new ArrayList<>(seleccionados);
Collections.sort(lista);
StringBuilder sb=new StringBuilder("Días seleccionados: ");
for(int i=0;i<lista.size();i++){sb.append(lista.get(i));if(i<lista.size()-1)sb.append(", ");}
diasSeleccionados.setText(sb.toString());
}
void buscar(){
if(seleccionados.isEmpty()){Toast.makeText(this,"Seleccione al menos un día",Toast.LENGTH_SHORT).show();return;}
List<Integer> lista=new ArrayList<>(seleccionados);
Collections.sort(lista);
final StringBuilder titulo=new StringBuilder("Días: ");
for(int i=0;i<lista.size();i++){titulo.append(lista.get(i));if(i<lista.size()-1)titulo.append(", ");if(i>=15){titulo.append("...");break;}}
new Thread(()->{
List<Draw> todos=leerTodos();
int[][] cntFijo=new int[100][1];int[][] cntC1=new int[100][1];int[][] cntC2=new int[100][1];
int[] cntDec=new int[10];int[] cntTerm=new int[10];
int encontrados=0;
for(Draw d:todos){
int dia=extraerDia(d.date);
if(dia<0)continue;
if(!seleccionados.contains(dia))continue;
encontrados++;
int f=parse(d.f);int c1=parse(d.c1);int c2=parse(d.c2);
if(f>=0){cntFijo[f][0]++;cntDec[f/10]++;cntTerm[f%10]++;}
if(c1>=0)cntC1[c1][0]++;
if(c2>=0)cntC2[c2][0]++;
}
StringBuilder res=new StringBuilder();
res.append(titulo).append("\n");
res.append("Sorteos encontrados: ").append(encontrados).append("\n\n");
res.append("🔵 FIJO (top 10):\n").append(top10(cntFijo)).append("\n\n");
res.append("🟢 CORRIDO 1 (top 10):\n").append(top10(cntC1)).append("\n\n");
res.append("🟠 CORRIDO 2 (top 10):\n").append(top10(cntC2)).append("\n\n");
res.append("🟣 DECENAS (top 10):\n").append(top10Dig(cntDec)).append("\n\n");
res.append("🟡 TERMINALES (top 10):\n").append(top10Dig(cntTerm));
final String r=res.toString();
runOnUiThread(()->mostrarResultado(titulo.toString(),r));
}).start();
}
int extraerDia(String fecha){try{String[] p=fecha.split("[-/]");return Integer.parseInt(p[0]);}catch(Exception e){return -1;}}
int parse(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
String top10(int[][] arr){
int n=arr.length;
int[] ix=new int[n];for(int i=0;i<n;i++)ix[i]=i;
for(int i=0;i<n;i++)for(int j=i+1;j<n;j++)if(arr[ix[j]][0]>arr[ix[i]][0]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}
StringBuilder sb=new StringBuilder();
for(int i=0;i<10&&i<n;i++){if(arr[ix[i]][0]==0)break;sb.append(String.format("%02d",ix[i])).append("(").append(arr[ix[i]][0]).append(") ");}
return sb.length()==0?"(sin datos)":sb.toString();
}
String top10Dig(int[] arr){
int n=arr.length;
int[] ix=new int[n];for(int i=0;i<n;i++)ix[i]=i;
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
c.close();db.close();
return lista;
}
void mostrarResultado(String titulo,String cuerpo){
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
