package com.lotelab.analizador;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.net.Uri;import android.provider.OpenableColumns;import android.view.*;import android.widget.*;import android.graphics.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.text.*;import java.util.*;import java.util.zip.*;import javax.xml.parsers.*;import org.w3c.dom.*;
public class MainActivity extends Activity{
DB db;TextView estado,salida,panelHoy,sugerencia,aciertos,calientes,frios,sesgos;TableLayout tabla;static final int PICK=10;
@Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);db=new DB(this);estado=findViewById(R.id.estado);salida=findViewById(R.id.salida);tabla=findViewById(R.id.tablaResultados);panelHoy=findViewById(R.id.panelHoy);sugerencia=findViewById(R.id.sugerencia);aciertos=findViewById(R.id.aciertos);calientes=findViewById(R.id.calientes);frios=findViewById(R.id.frios);sesgos=findViewById(R.id.sesgos);
findViewById(R.id.btnImportar).setOnClickListener(v->pick());
findViewById(R.id.btnNuevo).setOnClickListener(v->nuevo());
findViewById(R.id.btnCalcular).setOnClickListener(v->calcular());
findViewById(R.id.btnHistorial).setOnClickListener(v->verHistorial());
findViewById(R.id.btnResultados).setOnClickListener(v->calcular());
findViewById(R.id.btnExportar).setOnClickListener(v->exportar());
if(!getPreferences(0).getBoolean("init",false)){new Thread(()->{try{InputStream in=getAssets().open("Florida_inicial.tsv");int n=Importer.importStream(db,in,"Florida_inicial.tsv");getPreferences(0).edit().putBoolean("init",true).apply();runOnUiThread(()->estado.setText("Historial cargado: "+n));}catch(Exception e){runOnUiThread(()->estado.setText("Error: "+e.getMessage()));}}).start();}else estado.setText("Sorteos: "+db.count());}
void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,PICK);}
@Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==PICK&&c==RESULT_OK&&d!=null){Uri u=d.getData();new Thread(()->{try{InputStream in=getContentResolver().openInputStream(u);int n=Importer.importStream(db,in,getName(u));runOnUiThread(()->estado.setText("Importados: "+n+" Total: "+db.count()));}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());}}).start();}}
String getName(Uri u){Cursor c=getContentResolver().query(u,null,null,null,null);if(c!=null){try{int x=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(c.moveToFirst()&&x>=0)return c.getString(x);}finally{c.close();}}return "archivo";}
void nuevo(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);String[] h={"Fecha dd-MM-yyyy","T/N","Centena","Fijo","Corrido 1","Corrido 2"};EditText[] e=new EditText[6];for(int i=0;i<6;i++){e[i]=new EditText(this);e[i].setHint(h[i]);l.addView(e[i]);}new AlertDialog.Builder(this).setTitle("Agregar sorteo").setView(l).setPositiveButton("Guardar",(x,w)->{try{db.insert(new Draw(e[0].getText().toString(),e[1].getText().toString(),e[2].getText().toString(),pad(e[3].getText().toString()),pad(e[4].getText().toString()),pad(e[5].getText().toString())));estado.setText("Total: "+db.count());}catch(Exception z){Toast.makeText(this,"Error: "+z.getMessage(),Toast.LENGTH_LONG).show();}}).setNegativeButton("Cancelar",null).show();}
String pad(String s){s=s.trim();if(s.length()==1)s="0"+s;return s;}
void calcular(){new Thread(()->{Stats s=Stats.calc(db);runOnUiThread(()->{dibujarTabla(s);mostrarHoy(s);mostrarCalientes(s);mostrarFrios(s);mostrarSesgos(s);estado.setText("Listo. "+db.count()+" sorteos.");});}).start();}
void mostrarHoy(Stats s){
Calendar cal=Calendar.getInstance();
int diaHoy=cal.get(Calendar.DAY_OF_MONTH);
String fechaHoy=new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy",new Locale("es","ES")).format(cal.getTime());
int rangoHoy=rangeDelDia(diaHoy);
StringBuilder h=new StringBuilder();
h.append("HOY: ").append(fechaHoy).append(" | ");
h.append("Dia ").append(diaHoy).append(" -> Rango ").append(Stats.R[rangoHoy]).append(" | ");
h.append("Sorteos en este rango: ").append(s.sorteosPorRango[rangoHoy]).append(" | ");
int ultDia=s.ultimoDia;
if(ultDia>0){h.append("Ultimo sorteo: dia ").append(ultDia).append(" (rango ").append(Stats.R[rangeDelDia(ultDia)]).append(")");}
panelHoy.setText(h.toString());
StringBuilder g=new StringBuilder();
g.append("SUGERENCIA PARA HOY (rango ").append(Stats.R[rangoHoy]).append("):\n");
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
void mostrarCalientes(Stats s){
StringBuilder c=new StringBuilder();
c.append("🔥 TOP 10 NUMEROS CALIENTES (Fijo - historial completo):\n");
c.append(s.top10Calientes);
calientes.setText(c.toString());
}
void mostrarFrios(Stats s){
StringBuilder f=new StringBuilder();
f.append("❄️ TOP 10 NUMEROS FRIOS (Fijo - historial completo):\n");
f.append(s.top10Frios);
frios.setText(f.toString());
}
void mostrarSesgos(Stats s){
StringBuilder g=new StringBuilder();
g.append("📊 SESGO POR DECENA (mas frecuentes primero):\n");
g.append(s.sesgoDecenas).append("\n");
g.append("📊 SESGO POR TERMINAL (mas frecuentes primero):\n");
g.append(s.sesgoTerminales);
sesgos.setText(g.toString());
}
int rangeDelDia(int dia){if(dia>=1&&dia<=5)return 0;if(dia>=6&&dia<=10)return 1;if(dia>=11&&dia<=15)return 2;if(dia>=16&&dia<=20)return 3;if(dia>=21&&dia<=25)return 4;return 5;}
void dibujarTabla(Stats s){tabla.removeAllViews();tabla.setBackgroundColor(Color.parseColor("#9E9E9E"));
String[] headers={"Rango","Fijo","Corrido 1","Corrido 2","Decenas","Terminales"};
String[] colores={"#FFFFFF","#1565C0","#2E7D32","#EF6C00","#6A1B9A","#F9A825"};
TableRow encabezado=new TableRow(this);
for(int i=0;i<6;i++){encabezado.addView(celda(headers[i],colores[i],Color.WHITE,true));}
tabla.addView(encabezado);
for(int r=0;r<6;r++){TableRow fila=new TableRow(this);
fila.addView(celda(Stats.R[r],"#FFFFFF",Color.parseColor("#1A237E"),true));
fila.addView(celda(s.topsFijo[r],"#FFFFFF",Color.parseColor("#0D47A1"),false));
fila.addView(celda(s.topsC1[r],"#FFFFFF",Color.parseColor("#1B5E20"),false));
fila.addView(celda(s.topsC2[r],"#FFFFFF",Color.parseColor("#E65100"),false));
fila.addView(celda(s.topsDec[r],"#FFFFFF",Color.parseColor("#4A148C"),false));
fila.addView(celda(s.topsTerm[r],"#FFFFFF",Color.parseColor("#F57F17"),false));
tabla.addView(fila);}}
TextView celda(String txt,String bg,int colorTexto,boolean negrita){TextView tv=new TextView(this);tv.setText(txt);tv.setPadding(8,8,8,8);tv.setTextSize(11);tv.setTextColor(colorTexto);tv.setBackgroundColor(Color.parseColor(bg));if(negrita)tv.setTypeface(null,Typeface.BOLD);return tv;}
void verHistorial(){StringBuilder b=new StringBuilder();Cursor c=db.all();int n=0;while(c.moveToNext()&&n<100){b.append(c.getString(1)).append(" | ").append(c.getString(2)).append(" | ").append(c.getString(3)).append(" | ").append(c.getString(4)).append(" | ").append(c.getString(5)).append(" | ").append(c.getString(6)).append(" -- ");n++;}c.close();salida.setText("Primeros "+n+": "+b);}
void exportar(){try{File f=new File(getExternalFilesDir(null),"historial.csv");FileWriter w=new FileWriter(f);w.write("Fecha,TN,Centena,Fijo,C1,C2\n");Cursor c=db.all();while(c.moveToNext())w.write(c.getString(1)+","+c.getString(2)+","+c.getString(3)+","+c.getString(4)+","+c.getString(5)+","+c.getString(6)+"\n");c.close();w.close();new AlertDialog.Builder(this).setTitle("Listo").setMessage(f.getAbsolutePath()).setPositiveButton("OK",null).show();}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}
static class Draw{String date,tn,cent,f,c1,c2;Draw(String a,String b,String c,String d,String e,String f){date=a;tn=b;cent=c;this.f=d;c1=e;c2=f;}}
static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"loteria.db",null,1);}public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE draws(id INTEGER PRIMARY KEY AUTOINCREMENT,date TEXT,tn TEXT,cent TEXT,fijo TEXT,c1 TEXT,c2 TEXT,UNIQUE(date,tn,fijo,c1,c2))");}public void onUpgrade(SQLiteDatabase d,int o,int n){}void insert(Draw x){ContentValues v=new ContentValues();v.put("date",x.date);v.put("tn",x.tn);v.put("cent",x.cent);v.put("fijo",x.f);v.put("c1",x.c1);v.put("c2",x.c2);getWritableDatabase().insertWithOnConflict("draws",null,v,SQLiteDatabase.CONFLICT_IGNORE);}int count(){Cursor c=getReadableDatabase().rawQuery("select count(*) from draws",null);c.moveToFirst();int n=c.getInt(0);c.close();return n;}Cursor all(){return getReadableDatabase().query("draws",null,null,null,null,null,"id ASC");}}
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
String[] topsFijo=new String[6];String[] topsC1=new String[6];String[] topsC2=new String[6];String[] topsDec=new String[6];String[] topsTerm=new String[6];
String[] top5Fijo=new String[6];String[] top5C1=new String[6];String[] top5C2=new String[6];String[] top5Dec=new String[6];String[] top5Term=new String[6];
int[] sorteosPorRango=new int[6];
int[] acFijo=new int[6];int[] acC1=new int[6];int[] acC2=new int[6];int[] acDec=new int[6];int[] acTerm=new int[6];
String top10Calientes="";String top10Frios="";String sesgoDecenas="";String sesgoTerminales="";
int ultimoDia=0;
static int range(String date){try{String[] q=date.split("[-/]");int day=Integer.parseInt(q[0]);if(day>=1&&day<=5)return 0;if(day<=10)return 1;if(day<=15)return 2;if(day<=20)return 3;if(day<=25)return 4;return 5;}catch(Exception e){return -1;}}
static int num(String s){try{return Integer.parseInt(s);}catch(Exception e){return -1;}}
static String[] topN(int[] cnt,int n,String fmt){int[] ix=new int[cnt.length];for(int i=0;i<cnt.length;i++)ix[i]=i;for(int i=0;i<cnt.length;i++)for(int j=i+1;j<cnt.length;j++)if(cnt[ix[j]]>cnt[ix[i]]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}String[] res=new String[n];for(int k=0;k<n;k++){if(fmt.equals("num"))res[k]=String.format("%02d",ix[k])+"("+cnt[ix[k]]+")";else res[k]=ix[k]+"("+cnt[ix[k]]+")";}return res;}
static String[] bottomN(int[] cnt,int n,String fmt){int[] ix=new int[cnt.length];for(int i=0;i<cnt.length;i++)ix[i]=i;for(int i=0;i<cnt.length;i++)for(int j=i+1;j<cnt.length;j++)if(cnt[ix[j]]<cnt[ix[i]]){int t=ix[i];ix[i]=ix[j];ix[j]=t;}String[] res=new String[n];for(int k=0;k<n;k++){if(fmt.equals("num"))res[k]=String.format("%02d",ix[k])+"("+cnt[ix[k]]+")";else res[k]=ix[k]+"("+cnt[ix[k]]+")";}return res;}
static Stats calc(DB db){
int[][][] n=new int[6][3][100];
int[][][] dec=new int[6][3][10];
int[][][] term=new int[6][3][10];
int[] nGlobalFijo=new int[100];
int[] decGlobal=new int[10];
int[] termGlobal=new int[10];
int total=0;
Cursor c=db.all();
while(c.moveToNext()){
int r=range(c.getString(1));if(r<0)continue;
String[] a={c.getString(4),c.getString(5),c.getString(6)};
for(int k=0;k<3;k++){
int x=num(a[k]);
if(x>=0&&x<100){
n[r][k][x]++;
dec[r][k][x/10]++;
term[r][k][x%10]++;
if(k==0){nGlobalFijo[x]++;decGlobal[x/10]++;termGlobal[x%10]++;}
}
}
total++;
}
c.close();
Stats s=new Stats();
for(int r=0;r<6;r++){
String[] f=topN(n[r][0],3,"num");s.topsFijo[r]=f[0]+" "+f[1]+" "+f[2];
String[] c1=topN(n[r][1],3,"num");s.topsC1[r]=c1[0]+" "+c1[1]+" "+c1[2];
String[] c2=topN(n[r][2],3,"num");s.topsC2[r]=c2[0]+" "+c2[1]+" "+c2[2];
String[] d=topN(dec[r][0],3,"dig");s.topsDec[r]=d[0]+" "+d[1]+" "+d[2];
String[] t=topN(term[r][0],3,"dig");s.topsTerm[r]=t[0]+" "+t[1]+" "+t[2];
String[] f5=topN(n[r][0],5,"num");s.top5Fijo[r]=f5[0]+" "+f5[1]+" "+f5[2]+" "+f5[3]+" "+f5[4];
String[] c15=topN(n[r][1],5,"num");s.top5C1[r]=c15[0]+" "+c15[1]+" "+c15[2]+" "+c15[3]+" "+c15[4];
String[] c25=topN(n[r][2],5,"num");s.top5C2[r]=c25[0]+" "+c25[1]+" "+c25[2]+" "+c25[3]+" "+c25[4];
String[] d5=topN(dec[r][0],5,"dig");s.top5Dec[r]=d5[0]+" "+d5[1]+" "+d5[2]+" "+d5[3]+" "+d5[4];
String[] t5=topN(term[r][0],5,"dig");s.top5Term[r]=t5[0]+" "+t5[1]+" "+t5[2]+" "+t5[3]+" "+t5[4];
}
String[] cal=topN(nGlobalFijo,10,"num");StringBuilder cb=new StringBuilder();for(int i=0;i<10;i++){cb.append(cal[i]);if(i<9)cb.append(" ");}
s.top10Calientes=cb.toString();
String[] fri=bottomN(nGlobalFijo,10,"num");StringBuilder fb=new StringBuilder();for(int i=0;i<10;i++){fb.append(fri[i]);if(i<9)fb.append(" ");}
s.top10Frios=fb.toString();
String[] decS=topN(decGlobal,10,"dig");StringBuilder dsb=new StringBuilder();for(int i=0;i<10;i++){dsb.append(decS[i]);if(i<9)dsb.append(" ");}
s.sesgoDecenas=dsb.toString();
String[] termS=topN(termGlobal,10,"dig");StringBuilder tsb=new StringBuilder();for(int i=0;i<10;i++){tsb.append(termS[i]);if(i<9)tsb.append(" ");}
s.sesgoTerminales=tsb.toString();
Cursor c2=db.all();
while(c2.moveToNext()){
int r=range(c2.getString(1));if(r<0)continue;
s.sorteosPorRango[r]++;
int fVal=num(c2.getString(4)),c1Val=num(c2.getString(5)),c2Val=num(c2.getString(6));
if(fVal>=0){if(fVal==numTop1(n[r][0]))s.acFijo[r]++;if(fVal/10==digTop1(dec[r][0]))s.acDec[r]++;if(fVal%10==digTop1(term[r][0]))s.acTerm[r]++;}
if(c1Val>=0&&c1Val==numTop1(n[r][1]))s.acC1[r]++;
if(c2Val>=0&&c2Val==numTop1(n[r][2]))s.acC2[r]++;
int d=0;try{d=Integer.parseInt(c2.getString(1).split("[-/]")[0]);}catch(Exception ex){}
if(d>s.ultimoDia)s.ultimoDia=d;
}
c2.close();
return s;
}
static int numTop1(int[] cnt){int best=0;for(int i=1;i<cnt.length;i++)if(cnt[i]>cnt[best])best=i;return best;}
static int digTop1(int[] cnt){int best=0;for(int i=1;i<cnt.length;i++)if(cnt[i]>cnt[best])best=i;return best;}
}
}
