package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.net.Uri;import android.provider.OpenableColumns;import android.view.*;import android.widget.*;import android.graphics.*;import androidx.drawerlayout.widget.DrawerLayout;import java.io.*;import java.nio.charset.StandardCharsets;import java.text.*;import java.util.*;import java.util.zip.*;import javax.xml.parsers.*;import org.w3c.dom.*;
public class MainActivity extends Activity{
DB db;TextView estado,salida,panelHoy,sugerencia,aciertos,calientes,frios,sesgos,centenas,combinaciones,backtesting;TableLayout tabla;DrawerLayout drawerLayout;static final int PICK=10;static final int CREATE=20;
@Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);db=new DB(this);estado=findViewById(R.id.estado);salida=findViewById(R.id.salida);tabla=findViewById(R.id.tablaResultados);panelHoy=findViewById(R.id.panelHoy);sugerencia=findViewById(R.id.sugerencia);aciertos=findViewById(R.id.aciertos);calientes=findViewById(R.id.calientes);frios=findViewById(R.id.frios);sesgos=findViewById(R.id.sesgos);centenas=findViewById(R.id.centenas);combinaciones=findViewById(R.id.combinaciones);backtesting=findViewById(R.id.backtesting);
drawerLayout=findViewById(R.id.drawerLayout);
findViewById(R.id.btnMenu).setOnClickListener(v->drawerLayout.openDrawer(findViewById(R.id.menuLateral)));
findViewById(R.id.menuInicio).setOnClickListener(v->{drawerLayout.closeDrawers();});
findViewById(R.id.menuHistorial).setOnClickListener(v->{drawerLayout.closeDrawers();startActivity(new Intent(this,HistorialActivity.class));});
findViewById(R.id.menuBuscarDias).setOnClickListener(v->{drawerLayout.closeDrawers();startActivity(new Intent(this,BuscarDiasActivity.class));});
findViewById(R.id.menuBuscarNumeros).setOnClickListener(v->{drawerLayout.closeDrawers();startActivity(new Intent(this,BuscarNumerosActivity.class));});
findViewById(R.id.menuEstadisticas).setOnClickListener(v->{drawerLayout.closeDrawers();startActivity(new Intent(this,EstadisticasActivity.class));});
findViewById(R.id.menuPredicciones).setOnClickListener(v->{drawerLayout.closeDrawers();startActivity(new Intent(this,PrediccionesActivity.class));});
findViewById(R.id.menuGuardarPred).setOnClickListener(v->{drawerLayout.closeDrawers();guardarPrediccion();});
findViewById(R.id.menuImportar).setOnClickListener(v->{drawerLayout.closeDrawers();pick();});
findViewById(R.id.menuNuevo).setOnClickListener(v->{drawerLayout.closeDrawers();nuevo();});
findViewById(R.id.menuExportar).setOnClickListener(v->{drawerLayout.closeDrawers();exportar();});
findViewById(R.id.menuCalcular).setOnClickListener(v->{drawerLayout.closeDrawers();calcular();});
findViewById(R.id.menuBitmask).setOnClickListener(v->{drawerLayout.closeDrawers();reconstruirBitmask();});
findViewById(R.id.menuTestCumplimiento).setOnClickListener(v->{drawerLayout.closeDrawers();testCumplimiento();});
findViewById(R.id.menuMejoras).setOnClickListener(v->{drawerLayout.closeDrawers();startActivity(new Intent(this,MejorasActivity.class));});
findViewById(R.id.menuAcerca).setOnClickListener(v->{drawerLayout.closeDrawers();new AlertDialog.Builder(this).setTitle("Acerca de").setMessage("Analizador de Loteria\nPick 3 · Pick 4 Florida\n\nVersion 1.2").setPositiveButton("OK",null).show();});
if(!getPreferences(0).getBoolean("init",false)){new Thread(()->{try{InputStream in=getAssets().open("Florida_inicial.tsv");int n=Importer.importStream(db,in,"Florida_inicial.tsv");getPreferences(0).edit().putBoolean("init",true).apply();
int filas=BitmaskBuilder.poblarBitmask(db.getWritableDatabase());
final int fn=n;final int ff=filas;
runOnUiThread(()->estado.setText("Historial cargado: "+fn+" | Bitmask: "+ff));
}catch(Exception e){runOnUiThread(()->estado.setText("Error: "+e.getMessage()));}}).start();}
else{
estado.setText("Historial: "+db.count()+" sorteos");
new Thread(()->{
try{
int filas=BitmaskBuilder.contarFilas(db.getReadableDatabase());
if(filas<=0){
int f=BitmaskBuilder.poblarBitmask(db.getWritableDatabase());
runOnUiThread(()->estado.setText("Historial: "+db.count()+" sorteos | Bitmask: "+f));
}else{
final int f=filas;
runOnUiThread(()->estado.setText("Historial: "+db.count()+" sorteos | Bitmask: "+f));
}
}catch(Exception e){}
}).start();
}
}
void testCumplimiento(){
new Thread(()->{
try{
Set<Integer> dias=new TreeSet<>();
dias.add(1);dias.add(2);dias.add(3);
CumplimientoResultado r=CumplimientoEngine.calcular(db.getReadableDatabase(),dias,"FIJO","AMBOS",0.50);
final String resumen=CumplimientoEngine.resumenCorto(r);
runOnUiThread(()->{
new AlertDialog.Builder(this).setTitle("🧪 Test Cumplimiento").setMessage(resumen).setPositiveButton("OK",null).setNeutralButton("Copiar",(d,w)->{
ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
cm.setPrimaryClip(ClipData.newPlainText("cumplimiento",resumen));
Toast.makeText(this,"Copiado al portapapeles",Toast.LENGTH_SHORT).show();
}).show();
});
}catch(final Exception e){
runOnUiThread(()->{
new AlertDialog.Builder(this).setTitle("Error").setMessage(e.toString()+"\n\n"+(e.getMessage()!=null?e.getMessage():"")).setPositiveButton("OK",null).show();
});
}
}).start();
}
void reconstruirBitmask(){
new Thread(()->{
try{
runOnUiThread(()->estado.setText("Reconstruyendo bitmask..."));
int filas=BitmaskBuilder.poblarBitmask(db.getWritableDatabase());
final int f=filas;
runOnUiThread(()->{
estado.setText("Historial: "+db.count()+" sorteos | Bitmask: "+f);
Toast.makeText(this,"Bitmask actualizada: "+f+" filas",Toast.LENGTH_LONG).show();
});
}catch(Exception e){
runOnUiThread(()->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());
}
}).start();
}
void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,PICK);}
@Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d==null)return;
if(r==PICK){Uri u=d.getData();new Thread(()->{try{InputStream in=getContentResolver().openInputStream(u);int n=Importer.importStream(db,in,getName(u));new Thread(this::validarPrediccionesAuto).start();
runOnUiThread(()->estado.setText("Importados: "+n+" | Total: "+db.count()+"\nActualizando bitmask..."));
int filas=BitmaskBuilder.poblarBitmask(db.getWritableDatabase());
final int nf=n;final int ff=filas;
runOnUiThread(()->{
estado.setText("Importados: "+nf+" | Total: "+db.count()+" | Bitmask: "+ff);
Toast.makeText(this,"Bitmask actualizada: "+ff+" filas",Toast.LENGTH_LONG).show();
});
}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());}}).start();}
else if(r==CREATE){Uri u=d.getData();new Thread(()->{try{OutputStream out=getContentResolver().openOutputStream(u);if(out==null){runOnUiThread(()->Toast.makeText(this,"No se pudo abrir el archivo",Toast.LENGTH_LONG).show());return;}
Writer w=new OutputStreamWriter(out,StandardCharsets.UTF_8);
w.write("Fecha,TN,Centena,Fijo,C1,C2\n");
Cursor cur=db.all();
while(cur.moveToNext())w.write(cur.getString(1)+","+cur.getString(2)+","+cur.getString(3)+","+cur.getString(4)+","+cur.getString(5)+","+cur.getString(6)+"\n");
cur.close();w.close();
runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Listo").setMessage("Archivo CSV guardado correctamente").setPositiveButton("OK",null).show());
}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());}}).start();}}
String getName(Uri u){Cursor c=getContentResolver().query(u,null,null,null,null);if(c!=null){try{int x=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(c.moveToFirst()&&x>=0)return c.getString(x);}finally{c.close();}}return "archivo";}
void nuevo(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);String[] h={"Fecha dd-MM-yyyy","T/N","Centena","Fijo","Corrido 1","Corrido 2"};EditText[] e=new EditText[6];for(int i=0;i<6;i++){e[i]=new EditText(this);e[i].setHint(h[i]);l.addView(e[i]);}new AlertDialog.Builder(this).setTitle("Agregar sorteo").setView(l).setPositiveButton("Guardar",(x,w)->{try{db.insert(new Draw(e[0].getText().toString(),e[1].getText().toString(),e[2].getText().toString(),pad(e[3].getText().toString()),pad(e[4].getText().toString()),pad(e[5].getText().toString())));estado.setText("Total: "+db.count());new Thread(this::validarPrediccionesAuto).start();
new Thread(()->{try{int f=BitmaskBuilder.poblarBitmask(db.getWritableDatabase());final int ff=f;runOnUiThread(()->Toast.makeText(this,"Bitmask actualizada: "+ff+" filas",Toast.LENGTH_SHORT).show());}catch(Exception ex){}}).start();
}catch(Exception z){Toast.makeText(this,"Error: "+z.getMessage(),Toast.LENGTH_LONG).show();}}).setNegativeButton("Cancelar",null).show();}
String pad(String s){s=s.trim();if(s.length()==1)s="0"+s;return s;}
void calcular(){new Thread(()->{try{List<Draw> lista=leerTodos();Stats s=Stats.calc(lista);runOnUiThread(()->{dibujarTabla(s);mostrarHoy(s,lista);mostrarCalientes(s);mostrarFrios(s);mostrarSesgos(s);mostrarCentenas(s);mostrarCombinaciones(s);mostrarBacktesting(s);estado.setText("Listo. "+lista.size()+" sorteos procesados.");});}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());}}).start();}
List<Draw> leerTodos(){List<Draw> lista=new ArrayList<>();Cursor c=db.all();while(c.moveToNext()){lista.add(new Draw(c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6)));}c.close();return lista;}
void guardarPrediccion(){
new Thread(()->{
try{
List<Draw> lista=leerTodos();
Stats s=Stats.calc(lista);
Calendar cal=Calendar.getInstance();
String fechaHoy=new SimpleDateFormat("dd-MM-yyyy",new Locale("es","ES")).format(cal.getTime());
int rangoHoy=Stats.rangeDelDia(cal.get(Calendar.DAY_OF_MONTH));
String rangoStr=Stats.R[rangoHoy];
DBHelperAux aux=new DBHelperAux(this);
ContentValues v=new ContentValues();
v.put("fecha",fechaHoy);
v.put("rango",rangoStr);
v.put("top5Fijo",s.top5Fijo[rangoHoy]);
v.put("top5C1",s.top5C1[rangoHoy]);
v.put("top5C2",s.top5C2[rangoHoy]);
v.put("top5Dec",s.top5Dec[rangoHoy]);
v.put("top5Term",s.top5Term[rangoHoy]);
aux.getWritableDatabase().insert("predicciones",null,v);
aux.close();
runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Guardado").setMessage("Prediccion guardada para: "+fechaHoy+"\nRango: "+rangoStr).setPositiveButton("OK",null).show());
}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());}
}).start();
}
void validarPrediccionesAuto(){
try{
DBHelperAux aux=new DBHelperAux(this);
Cursor preds=aux.getReadableDatabase().rawQuery("SELECT id,fecha,top5Fijo,top5C1,top5C2,top5Dec,top5Term FROM predicciones WHERE validada=0",null);
List<Integer> ids=new ArrayList<>();List<String> fechas=new ArrayList<>();List<String> topsF=new ArrayList<>();List<String> topsC1=new ArrayList<>();List<String> topsC2=new ArrayList<>();List<String> topsD=new ArrayList<>();List<String> topsT=new ArrayList<>();
while(preds.moveToNext()){ids.add(preds.getInt(0));fechas.add(preds.getString(1));topsF.add(preds.getString(2));topsC1.add(preds.getString(3));topsC2.add(preds.getString(4));topsD.add(preds.getString(5));topsT.add(preds.getString(6));}
preds.close();
for(int i=0;i<ids.size();i++){
String[] fF=topsF.get(i).split("\\s+");String[] fC1=topsC1.get(i).split("\\s+");String[] fC2=topsC2.get(i).split("\\s+");String[] fD=topsD.get(i).split("\\s+");String[] fT=topsT.get(i).split("\\s+");
Cursor sorteos=aux.getReadableDatabase().rawQuery("SELECT fijo,c1,c2,cent FROM draws WHERE date='"+fechas.get(i)+"'",null);
while(sorteos.moveToNext()){
String rFijo=sorteos.getString(0);String rC1=sorteos.getString(1);String rC2=sorteos.getString(2);String rCent=sorteos.getString(3);
int aF=0,aC1=0,aC2=0,aD=0,aT=0;
if(enTop(rFijo,fF))aF=1;if(enTop(rC1,fC1))aC1=1;if(enTop(rC2,fC2))aC2=1;
int cent=parseInt(rCent);if(cent>=0&&enTopDig(cent,fD))aD=1;
int term=parseInt(rFijo);if(term>=0&&enTopDig(term%10,fT))aT=1;
ContentValues v=new ContentValues();
v.put("resultadoFijo",rFijo);v.put("resultadoC1",rC1);v.put("resultadoC2",rC2);v.put("resultadoDec",rCent);v.put("resultadoTerm",String.valueOf(term%10));
v.put("aciertosFijo",aF);v.put("aciertosC1",aC1);v.put("aciertosC2",aC2);v.put("aciertosDec",aD);v.put("aciertosTerm",aT);v.put("validada",1);
aux.getWritableDatabase().update("predicciones",v,"id=?",new String[]{String.valueOf(ids.get(i))});
break;
}
sorteos.close();
}
aux.close();
}catch(Exception e){e.printStackTrace();}
}
boolean enTop(String num,String[] top5){for(String t:top5){String n=t.split("\\(")[0];if(n.equals(num))return true;}return false;}
boolean enTopDig(int dig,String[] top5){for(String t:top5){String n=t.split("\\(")[0];try{if(Integer.parseInt(n)==dig)return true;}catch(Exception e){}}return false;}
int parseInt(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
void mostrarHoy(Stats s,List<Draw> lista){
Calendar cal=Calendar.getInstance();
int diaHoy=cal.get(Calendar.DAY_OF_MONTH);
String fechaHoy=new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy",new Locale("es","ES")).format(cal.getTime());
int rangoHoy=Stats.rangeDelDia(diaHoy);
StringBuilder h=new StringBuilder();
h.append("HOY: ").append(fechaHoy).append(" | ");
h.append("Dia ").append(diaHoy).append(" -> Rango ").append(Stats.R[rangoHoy]).append(" | ");
h.append("Total sorteos: ").append(lista.size()).append(" | ");
h.append("En este rango: ").append(s.sorteosPorRango[rangoHoy]);
panelHoy.setText(h.toString());
StringBuilder g=new StringBuilder();
g.append("SUGERENCIA PARA HOY (rango ").append(Stats.R[rangoHoy]).append("):\n");
g.append("CENTENA: ").append(s.top5Cent[rangoHoy]).append("\n");
g.append("FIJO: ").append(s.top5Fijo[rangoHoy]).append("\n");
g.append("CORRIDO 1: ").append(s.top5C1[rangoHoy]).append("\n");
g.append("CORRIDO 2: ").append(s.top5C2[rangoHoy]).append("\n");
g.append("DECENAS: ").append(s.top5Dec[rangoHoy]).append("\n");
g.append("TERMINALES: ").append(s.top5Term[rangoHoy]);
sugerencia.setText(g.toString());
StringBuilder a=new StringBuilder();
a.append("ACIERTOS HISTORICOS (sorteos del rango ").append(Stats.R[rangoHoy]).append("):\n");
a.append("Fijo: ").append(s.acFijo[rangoHoy]).append(" | ");
a.append("C1: ").append(s.acC1[rangoHoy]).append(" | ");
a.append("C2: ").append(s.acC2[rangoHoy]).append(" | ");
a.append("Dec: ").append(s.acDec[rangoHoy]).append(" | ");
a.append("Term: ").append(s.acTerm[rangoHoy]);
aciertos.setText(a.toString());
}
void mostrarCalientes(Stats s){StringBuilder c=new StringBuilder();c.append("CALIENTES HISTORICO:\n").append(s.top10CalientesHist).append("\nCALIENTES RECIENTE:\n").append(s.top10CalientesRec).append("\nROBUSTOS:\n").append(s.robustosCalientes);calientes.setText(c.toString());}
void mostrarFrios(Stats s){StringBuilder f=new StringBuilder();f.append("FRIOS HISTORICO:\n").append(s.top10FriosHist).append("\nFRIOS RECIENTE:\n").append(s.top10FriosRec);frios.setText(f.toString());}
void mostrarSesgos(Stats s){StringBuilder g=new StringBuilder();g.append("DECENAS HIST:\n").append(s.sesgoDecHist).append("\nDECENAS REC:\n").append(s.sesgoDecRec).append("\nTERM HIST:\n").append(s.sesgoTermHist).append("\nTERM REC:\n").append(s.sesgoTermRec);sesgos.setText(g.toString());}
void mostrarCentenas(Stats s){StringBuilder c=new StringBuilder();c.append("CENTENAS HIST:\n").append(s.sesgoCentHist).append("\nCENTENAS REC:\n").append(s.sesgoCentRec).append("\nCALIENTES:\n").append(s.topCentCalientes).append("\nFRIAS:\n").append(s.topCentFrias);centenas.setText(c.toString());}
void mostrarCombinaciones(Stats s){StringBuilder c=new StringBuilder();c.append("COMBINACIONES:\n").append(s.combinaciones);combinaciones.setText(c.toString());}
void mostrarBacktesting(Stats s){StringBuilder b=new StringBuilder();b.append("BACKTESTING:\nTrain: ").append(s.backtestTrainingSize).append("\nTest: ").append(s.backtestTestingSize).append("\nAciertos: ").append(s.backtestAciertos);if(s.backtestTestingSize>0){b.append("\nPct: ").append((s.backtestAciertos*100)/s.backtestTestingSize).append("%");}backtesting.setText(b.toString());}
void dibujarTabla(Stats s){tabla.removeAllViews();tabla.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] headers={"Rango","Centena","Fijo","C1","C2","Dec","Term"};
String[] colores={"#FFFFFF","#8D6E63","#1565C0","#2E7D32","#EF6C00","#6A1B9A","#F9A825"};
TableRow encabezado=new TableRow(this);
for(int i=0;i<7;i++){encabezado.addView(celda(headers[i],colores[i],Color.WHITE,true));}
tabla.addView(encabezado);
for(int r=0;r<6;r++){TableRow fila=new TableRow(this);
fila.addView(celda(Stats.R[r],"#FFFFFF",Color.parseColor("#1A237E"),true));
fila.addView(celda(s.topsCent[r],"#FFFFFF",Color.parseColor("#4E342E"),false));
fila.addView(celda(s.topsFijo[r],"#FFFFFF",Color.parseColor("#0D47A1"),false));
fila.addView(celda(s.topsC1[r],"#FFFFFF",Color.parseColor("#1B5E20"),false));
fila.addView(celda(s.topsC2[r],"#FFFFFF",Color.parseColor("#E65100"),false));
fila.addView(celda(s.topsDec[r],"#FFFFFF",Color.parseColor("#4A148C"),false));
fila.addView(celda(s.topsTerm[r],"#FFFFFF",Color.parseColor("#F57F17"),false));
tabla.addView(fila);}}
TextView celda(String txt,String bg,int colorTexto,boolean negrita){TextView tv=new TextView(this);tv.setText(txt);tv.setPadding(6,8,6,8);tv.setTextSize(10);tv.setTextColor(colorTexto);tv.setBackgroundColor(Color.parseColor(bg));if(negrita)tv.setTypeface(null,Typeface.BOLD);return tv;}
void exportar(){Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("text/csv");intent.putExtra(Intent.EXTRA_TITLE,"historial_loteria.csv");startActivityForResult(intent,CREATE);}
static class Draw{String date,tn,cent,f,c1,c2;Draw(String a,String b,String c,String d,String e,String f){date=a;tn=b;cent=c;this.f=d;c1=e;c2=f;}}
static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"loteria.db",null,3);}public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE draws(id INTEGER PRIMARY KEY AUTOINCREMENT,date TEXT,tn TEXT,cent TEXT,fijo TEXT,c1 TEXT,c2 TEXT,UNIQUE(date,tn,fijo,c1,c2))");d.execSQL("CREATE TABLE IF NOT EXISTS predicciones(id INTEGER PRIMARY KEY AUTOINCREMENT,fecha TEXT,rango TEXT,top5Fijo TEXT,top5C1 TEXT,top5C2 TEXT,top5Dec TEXT,top5Term TEXT,resultadoFijo TEXT DEFAULT '',resultadoC1 TEXT DEFAULT '',resultadoC2 TEXT DEFAULT '',resultadoDec TEXT DEFAULT '',resultadoTerm TEXT DEFAULT '',aciertosFijo INTEGER DEFAULT 0,aciertosC1 INTEGER DEFAULT 0,aciertosC2 INTEGER DEFAULT 0,aciertosDec INTEGER DEFAULT 0,aciertosTerm INTEGER DEFAULT 0,validada INTEGER DEFAULT 0)");d.execSQL("CREATE TABLE IF NOT EXISTS cumplimiento_bitmask(categoria TEXT NOT NULL,turno TEXT NOT NULL,valor INTEGER NOT NULL,anio INTEGER NOT NULL,mes INTEGER NOT NULL,bitmask INTEGER NOT NULL,aciertos_count INTEGER NOT NULL,PRIMARY KEY(categoria,turno,valor,anio,mes))");d.execSQL("CREATE INDEX IF NOT EXISTS idx_cumplimiento_cat_valor ON cumplimiento_bitmask(categoria,valor)");d.execSQL("CREATE INDEX IF NOT EXISTS idx_cumplimiento_anio_mes ON cumplimiento_bitmask(anio,mes)");}public void onUpgrade(SQLiteDatabase d,int o,int n){if(o<2){d.execSQL("CREATE TABLE IF NOT EXISTS predicciones(id INTEGER PRIMARY KEY AUTOINCREMENT,fecha TEXT,rango TEXT,top5Fijo TEXT,top5C1 TEXT,top5C2 TEXT,top5Dec TEXT,top5Term TEXT,resultadoFijo TEXT DEFAULT '',resultadoC1 TEXT DEFAULT '',resultadoC2 TEXT DEFAULT '',resultadoDec TEXT DEFAULT '',resultadoTerm TEXT DEFAULT '',aciertosFijo INTEGER DEFAULT 0,aciertosC1 INTEGER DEFAULT 0,aciertosC2 INTEGER DEFAULT 0,aciertosDec INTEGER DEFAULT 0,aciertosTerm INTEGER DEFAULT 0,validada INTEGER DEFAULT 0)");}if(o<3){d.execSQL("CREATE TABLE IF NOT EXISTS cumplimiento_bitmask(categoria TEXT NOT NULL,turno TEXT NOT NULL,valor INTEGER NOT NULL,anio INTEGER NOT NULL,mes INTEGER NOT NULL,bitmask INTEGER NOT NULL,aciertos_count INTEGER NOT NULL,PRIMARY KEY(categoria,turno,valor,anio,mes))");d.execSQL("CREATE INDEX IF NOT EXISTS idx_cumplimiento_cat_valor ON cumplimiento_bitmask(categoria,valor)");d.execSQL("CREATE INDEX IF NOT EXISTS idx_cumplimiento_anio_mes ON cumplimiento_bitmask(anio,mes)");}}void insert(Draw x){ContentValues v=new ContentValues();v.put("date",x.date);v.put("tn",x.tn);v.put("cent",x.cent);v.put("fijo",x.f);v.put("c1",x.c1);v.put("c2",x.c2);getWritableDatabase().insertWithOnConflict("draws",null,v,SQLiteDatabase.CONFLICT_IGNORE);}int count(){Cursor c=getReadableDatabase().rawQuery("select count(*) from draws",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}Cursor all(){return getReadableDatabase().query("draws",null,null,null,null,null,"id ASC");}}
static class DBHelperAux extends SQLiteOpenHelper{DBHelperAux(Context c){super(c,"loteria.db",null,3);}public void onCreate(SQLiteDatabase d){}public void onUpgrade(SQLiteDatabase d,int o,int n){}}
static class Importer{
static int importStream(DB db,InputStream in,String name)throws Exception{byte[] data=readAll(in);String lower=name.toLowerCase();if(isZip(data)){if(lower.endsWith(".xlsx"))return xlsx(db,data);if(lower.endsWith(".docx"))return docx(db,data);}return text(db,new String(data,StandardCharsets.UTF_8));}
static boolean isZip(byte[] d){return d.length>4&&d[0]=='P'&&d[1]=='K';}
static int text(DB db,String s){int n=0;String[] lines=s.split("\\r?\\n");for(String line:lines){String[] p=line.contains("\t")?line.split("\\t",-1):line.split("[,;]",-1);if(p.length<6)continue;if(p[0].toLowerCase().contains("fecha"))continue;String date=p[0].trim(),tn=p[1].trim(),cent=p[2].trim(),f=clean(p[3]),c1=clean(p[4]),c2=clean(p[5]);if(valid(date,f,c1,c2)){db.insert(new Draw(date,tn,cent,f,c1,c2));n++;}}return n;}
static String clean(String x){x=x.trim();x=x.replace("\"","");if(x.length()==1)x="0"+x;return x;}
static boolean valid(String date,String f,String c1,String c2){return date.length()>0&&f.matches("\\d{2}")&&c1.matches("\\d{2}")&&c2.matches("\\d{2}");}
static int xlsx(DB db,byte[] data)throws Exception{ZipInputStream z=new ZipInputStream(new ByteArrayInputStream(data));Map<String,String> files=new HashMap<>();ZipEntry e;while((e=z.getNextEntry())!=null){if(!e.isDirectory()&&(e.getName().equals("xl/sharedStrings.xml")||e.getName().matches("xl/worksheets/sheet\\d+\\.xml")))files.put(e.getName(),new String(readAll(z),StandardCharsets.UTF_8));}String ss=files.get("xl/sharedStrings.xml");List<String> shared=new ArrayList<>();if(ss!=null){Document d=xml(ss);NodeList ts=d.getElementsByTagName("t");for(int i=0;i<ts.getLength();i++)shared.add(ts.item(i).getTextContent());}String sheet=files.get("xl/worksheets/sheet1.xml");if(sheet==null)return 0;Document d=xml(sheet);NodeList rows=d.getElementsByTagName("row");int n=0;for(int i=0;i<rows.getLength();i++){NodeList cells=((Element)rows.item(i)).getElementsByTagName("c");Map<Integer,String> vals=new HashMap<>();for(int j=0;j<cells.getLength();j++){Element c=(Element)cells.item(j);String ref=c.getAttribute("r");int col=0;for(char ch:ref.toCharArray()){if(Character.isLetter(ch))col=col*26+(Character.toUpperCase(ch)-'A'+1);else break;}col--;NodeList v=c.getElementsByTagName("v");if(v.getLength()>0){String val=v.item(0).getTextContent();if("s".equals(c.getAttribute("t"))){int ix=Integer.parseInt(val);if(ix<shared.size())val=shared.get(ix);}vals.put(col,val);}}if(vals.size()>=6&&!vals.getOrDefault(0,"").toLowerCase().contains("fecha")){String date=vals.getOrDefault(0,"");String tn=vals.getOrDefault(1,"");String cent=vals.getOrDefault(2,"");String f=clean(vals.getOrDefault(3,"")),c1=clean(vals.getOrDefault(4,"")),c2=clean(vals.getOrDefault(5,""));if(valid(date,f,c1,c2)){db.insert(new Draw(date,tn,cent,f,c1,c2));n++;}}}return n;}
static int docx(DB db,byte[] data)throws Exception{ZipInputStream z=new ZipInputStream(new ByteArrayInputStream(data));String xml=null;ZipEntry e;while((e=z.getNextEntry())!=null){if(e.getName().equals("word/document.xml")){xml=new String(readAll(z),StandardCharsets.UTF_8);break;}}if(xml==null)return 0;Document d=xml(xml);StringBuilder b=new StringBuilder();NodeList ts=d.getElementsByTagName("t");for(int i=0;i<ts.getLength();i++)b.append(ts.item(i).getTextContent()).append(i%6==5?'\n':'\t');return text(db,b.toString());}
static Document xml(String s)throws Exception{DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();f.setNamespaceAware(true);return f.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));}
static byte[] readAll(InputStream in)throws IOException{ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] x=new byte[8192];int n;while((n=in.read(x))>0)b.write(x,0,n);return b.toByteArray();}}
static class Stats{
static String[] R={"1-5","6-10","11-15","16-20","21-25","26-31"};
String[] topsCent=new String[6];String[] topsFijo=new String[6];String[] topsC1=new String[6];String[] topsC2=new String[6];String[] topsDec=new String[6];String[] topsTerm=new String[6];
String[] top5Cent=new String[6];String[] top5Fijo=new String[6];String[] top5C1=new String[6];String[] top5C2=new String[6];String[] top5Dec=new String[6];String[] top5Term=new String[6];
int[] sorteosPorRango=new int[6];
int[] acFijo=new int[6];int[] acC1=new int[6];int[] acC2=new int[6];int[] acDec=new int[6];int[] acTerm=new int[6];
String top10CalientesHist="";String top10CalientesRec="";String robustosCalientes="";
String top10FriosHist="";String top10FriosRec="";
String sesgoDecHist="";String sesgoDecRec="";String sesgoTermHist="";String sesgoTermRec="";
String sesgoCentHist="";String sesgoCentRec="";String topCentCalientes="";String topCentFrias="";
String top3Dec="";String top3Term="";String combinaciones="";
String backtesting="";int backtestAciertos=0;int backtestTrainingSize=0;int backtestTestingSize=0;
int totalHist=0;int totalRec=0;
static int range(String date){try{String[] q=date.split("[-/]");int day=Integer.parseInt(q[0]);if(day>=1&&day<=5)return 0;if(day<=10)return 1;if(day<=15)return 2;if(day<=20)return 3;if(day<=25)return 4;return 5;}catch(Exception e){return -1;}}
static int rangeDelDia(int dia){if(dia>=1&&dia<=5)return 0;if(dia>=6&&dia<=10)return 1;if(dia>=11&&dia<=15)return 2;if(dia>=16&&dia<=20)return 3;if(dia>=21&&dia<=25)return 4;return 5;}
static int num(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
static String[] topN(int[] cnt,int n,String fmt){int[] ix=new int[cnt.length];for(int i=0;i<cnt.length;i++)ix[i]=i;for(int i=0;i<cnt.length;i++)for(int j=i+1;j<cnt.length;j++)if(cnt[ix[j]]>cnt[ix[i]]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}String[] res=new String[n];for(int k=0;k<n;k++){if(fmt.equals("num"))res[k]=String.format("%02d",ix[k])+"("+cnt[ix[k]]+")";else res[k]=ix[k]+"("+cnt[ix[k]]+")";}return res;}
static String[] bottomN(int[] cnt,int n,String fmt){int[] ix=new int[cnt.length];for(int i=0;i<cnt.length;i++)ix[i]=i;for(int i=0;i<cnt.length;i++)for(int j=i+1;j<cnt.length;j++)if(cnt[ix[j]]<cnt[ix[i]]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}String[] res=new String[n];for(int k=0;k<n;k++){if(fmt.equals("num"))res[k]=String.format("%02d",ix[k])+"("+cnt[ix[k]]+")";else res[k]=ix[k]+"("+cnt[ix[k]]+")";}return res;}
static String join(String[] arr){StringBuilder b=new StringBuilder();for(int i=0;i<arr.length;i++){b.append(arr[i]);if(i<arr.length-1)b.append(" ");}return b.toString();}
static Stats calc(List<Draw> lista){
Stats s=new Stats();
s.totalHist=lista.size();
int[][][] n=new int[6][3][100];
int[][][] dec=new int[6][3][10];
int[][][] term=new int[6][3][10];
int[][] cent=new int[6][10];
int[] nHistFijo=new int[100];
int[] decHist=new int[10];
int[] termHist=new int[10];
int[] centHist=new int[10];
for(Draw d:lista){
int r=range(d.date);if(r<0)continue;
int fVal=num(d.f);int c1Val=num(d.c1);int c2Val=num(d.c2);
int centVal=num(d.cent);
s.sorteosPorRango[r]++;
int[] vals={fVal,c1Val,c2Val};
for(int k=0;k<3;k++){int x=vals[k];if(x>=0&&x<100){n[r][k][x]++;dec[r][k][x/10]++;term[r][k][x%10]++;if(k==0){nHistFijo[x]++;decHist[x/10]++;termHist[x%10]++;}}}
if(centVal>=0&&centVal<10){cent[r][centVal]++;centHist[centVal]++;}
}
for(int r=0;r<6;r++){
s.topsCent[r]=join(topN(cent[r],3,"dig"));
s.topsFijo[r]=join(topN(n[r][0],3,"num"));
s.topsC1[r]=join(topN(n[r][1],3,"num"));
s.topsC2[r]=join(topN(n[r][2],3,"num"));
s.topsDec[r]=join(topN(dec[r][0],3,"dig"));
s.topsTerm[r]=join(topN(term[r][0],3,"dig"));
s.top5Cent[r]=join(topN(cent[r],5,"dig"));
s.top5Fijo[r]=join(topN(n[r][0],5,"num"));
s.top5C1[r]=join(topN(n[r][1],5,"num"));
s.top5C2[r]=join(topN(n[r][2],5,"num"));
s.top5Dec[r]=join(topN(dec[r][0],5,"dig"));
s.top5Term[r]=join(topN(term[r][0],5,"dig"));
}
s.top10CalientesHist=join(topN(nHistFijo,10,"num"));
s.top10FriosHist=join(bottomN(nHistFijo,10,"num"));
s.sesgoDecHist=join(topN(decHist,10,"dig"));
s.sesgoTermHist=join(topN(termHist,10,"dig"));
s.sesgoCentHist=join(topN(centHist,10,"dig"));
s.topCentCalientes=join(topN(centHist,5,"dig"));
s.topCentFrias=join(bottomN(centHist,5,"dig"));
int tamRec=Math.min(1000,lista.size());
s.totalRec=tamRec;
List<Draw> recientes=new ArrayList<>();
for(int i=lista.size()-tamRec;i<lista.size();i++)recientes.add(lista.get(i));
int[] nRecFijo=new int[100];int[] decRec=new int[10];int[] termRec=new int[10];int[] centRec=new int[10];
for(Draw d:recientes){int x=num(d.f);if(x>=0&&x<100){nRecFijo[x]++;decRec[x/10]++;termRec[x%10]++;}int c=num(d.cent);if(c>=0&&c<10)centRec[c]++;}
s.top10CalientesRec=join(topN(nRecFijo,10,"num"));
s.top10FriosRec=join(bottomN(nRecFijo,10,"num"));
s.sesgoDecRec=join(topN(decRec,10,"dig"));
s.sesgoTermRec=join(topN(termRec,10,"dig"));
s.sesgoCentRec=join(topN(centRec,10,"dig"));
List<String> rob=new ArrayList<>();
String[] calH=topN(nHistFijo,20,"num");String[] calR=topN(nRecFijo,20,"num");
for(String h:calH){String nH=h.split("\\(")[0];for(String rr:calR){if(rr.startsWith(nH+"(")){rob.add(h);break;}}if(rob.size()>=10)break;}
s.robustosCalientes=rob.isEmpty()?"(sin coincidencias)":join(rob.toArray(new String[0]));
String[] decT=topN(decRec,3,"dig");String[] termT=topN(termRec,3,"dig");
s.top3Dec=decT[0]+" "+decT[1]+" "+decT[2];
s.top3Term=termT[0]+" "+termT[1]+" "+termT[2];
String[] dTop={decT[0].split("\\(")[0],decT[1].split("\\(")[0],decT[2].split("\\(")[0]};
String[] tTop={termT[0].split("\\(")[0],termT[1].split("\\(")[0],termT[2].split("\\(")[0]};
StringBuilder cb=new StringBuilder();
for(String dd:dTop){for(String tt:tTop){cb.append(dd).append(tt).append("  ");}cb.append("\n");}
s.combinaciones=cb.toString();
int tamTrain=Math.min(1000,lista.size()/2);
int tamTest=Math.min(1000,lista.size()-tamTrain);
List<Draw> train=new ArrayList<>();
for(int i=0;i<tamTrain;i++)train.add(lista.get(i));
int[] nTrainFijo=new int[100];int[] decTrain=new int[10];int[] termTrain=new int[10];
for(Draw d:train){int x=num(d.f);if(x>=0&&x<100){nTrainFijo[x]++;decTrain[x/10]++;termTrain[x%10]++;}}
String[] decTr=topN(decTrain,3,"dig");String[] termTr=topN(termTrain,3,"dig");
List<String> combosTrain=new ArrayList<>();
for(int i=0;i<3;i++){String dd=decTr[i].split("\\(")[0];for(int j=0;j<3;j++){String tt=termTr[j].split("\\(")[0];combosTrain.add(dd+tt);}}
s.backtestTrainingSize=tamTrain;s.backtestTestingSize=tamTest;s.backtestAciertos=0;
for(int i=lista.size()-tamTest;i<lista.size();i++){Draw d=lista.get(i);int x=num(d.f);if(x<0)continue;String dosDig=String.format("%02d",x);if(combosTrain.contains(dosDig))s.backtestAciertos++;}
for(Draw d:lista){int r=range(d.date);if(r<0)continue;int fVal=num(d.f);if(fVal<0)continue;
if(fVal==numTop1(n[r][0]))s.acFijo[r]++;
if(fVal/10==digTop1(dec[r][0]))s.acDec[r]++;
if(fVal%10==digTop1(term[r][0]))s.acTerm[r]++;
int c1Val=num(d.c1);if(c1Val>=0&&c1Val==numTop1(n[r][1]))s.acC1[r]++;
int c2Val=num(d.c2);if(c2Val>=0&&numTop1(n[r][2])==c2Val)s.acC2[r]++;
}
return s;
}
static int numTop1(int[] cnt){int best=0;for(int i=1;i<cnt.length;i++)if(cnt[i]>cnt[best])best=i;return best;}
static int digTop1(int[] cnt){int best=0;for(int i=1;i<cnt.length;i++)if(cnt[i]>cnt[best])best=i;return best;}
}
}
