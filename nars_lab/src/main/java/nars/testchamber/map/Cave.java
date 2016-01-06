package nars.testchamber.map;

/**
 * 
 * @author Tyrant
 */
public class Cave {
	//
	// public static Map createCaves(int w, int h, int l) {
	// Map m=new Map(w,h);
	// initCaves(m,l,l);
	// m.set("Description","Dark Caves");
	// m.set("DungeonLevel",l);
	// return m;
	// }
	//
	// public static Map createBigCave(int w, int h, int level) {
	// Map m=new Map(w,h);
	// m.setTheme("caves");
	// m.set("WanderingRate",500);
	// m.set("Description","Sinister Cave");
	// m.set("EnterMessage","Your hear maniacal laughter...");
	// m.level(level);
	//
	// m.fillArea(0,0,w-1,h-1,m.wall());
	//
	// m.fillArea(w/2-3,h/2-3,w/2+3,h/2+3,m.floor());
	//
	// for (int i=0; i<((w+h)/3); i++) {
	// Point p = m.findFreeSquare();
	// int x1=RPG.rspread(1,w-2);
	// int y1=RPG.rspread(1,h-2);
	// m.makeRandomPath(p.x,p.y,x1,y1,1,1,w-2,h-2,m.floor(),false);
	// }
	//
	// for (int i=0; i<(w*h/80); i++) {
	// Point p = m.findFreeSquare();
	// int x1=p.x-Rand.d(6);
	// int y1=p.y-Rand.d(6);
	// int x2=p.x+Rand.d(6);
	// int y2=p.y+Rand.d(6);
	// if ((x1<1)||(x2>w-2)||(y1<1)||(y2>h-2)) continue;
	// m.fillOval(x1,y1,x2,y2,m.floor());
	// }
	//
	// Thing hero=Lib.create("Borrok");
	// hero.addThing(Lib.createArtifact(10));
	// m.addThing(hero);
	//
	// m.addEntrance("stairs up");
	//
	// return m;
	// }
	//
	// public static void initCaves (Map m, int t, int l ){
	//
	// m.setTheme("caves");
	//
	// m.level (2*l+4);
	//
	// switch (t) {
	// // top entrance of caves
	// case 1 : {
	// m.set("MonsterType","IsBeast");
	// m.fillArea(0, 0, 64, 64, m.wall());
	// m.fillArea(1, 1, 63, 63, 0);
	// m.fillArea(30, 30, 32, 32, m.floor());
	// makeCaves(m);
	//
	//
	// Thing down=Lib.create("ladder down");
	// down.set("ComplexName","caves");
	// down.set("DestinationLevel",2);
	// m.addThing(down);
	// break;
	// }
	//
	// // deeper caves level
	// case 2 : {
	// m.set("MonsterType","IsBeast");
	// m.fillArea(0, 0, 64, 64, m.wall());
	// m.fillArea(1, 1, 63, 63, 0);
	// m.fillArea(30, 30, 32, 32, m.floor());
	// makeCaves(m);
	//
	// Thing down=Lib.create("ladder down");
	// down.set("ComplexName","caves");
	// down.set("DestinationLevel",3);
	// m.addThing(down);
	//
	// m.addThing(Portal.create("mysterious dungeon"));
	//
	// break;
	// }
	//
	// // bandit's lair level
	// case 3 : {
	// m.set("MonsterType","IsBandit");
	// m.fillArea(0, 0, 64, 64, m.wall());
	// m.fillArea(1, 1, 63, 63, 0);
	// m.fillArea(27, 27, 35, 35, m.floor());
	// m.addThing(Fire.create(5), 27, 27, 35, 35);
	// m.addThing(Fire.create(5), 27, 27, 35, 35);
	//
	// // create the bandit leader.
	// // wayhey!
	//
	// Thing bandit = Lib.create("swordsman");
	// AI.name(bandit,"Graam the Terrible");
	// bandit.addThing(Lib.createArtifact(10));
	// m.addThing(bandit,27,27,35,35);
	//
	//
	// makeCaves(m);
	// break;
	// }
	//
	// default : {
	// break;
	// }
	// }
	//
	// m.setEntrance(Portal.create("ladder up"));
	// m.addThing(m.entrance());
	// }
	//
	// // Make the caves. Pure Art
	// public static void makeCaves(Map m) {
	// for (int buildloop = 0; buildloop < 200; buildloop++) {
	//
	// // first choose a direction
	// int dx = 0;
	// int dy = 0;
	// switch (Rand.d(4)) {
	// case 1 :
	// dx = 1;
	// break; //W
	// case 2 :
	// dx = -1;
	// break; //E
	// case 3 :
	// dy = 1;
	// break; //N
	// case 4 :
	// dy = -1;
	// break; //S
	// }
	//
	// // now find a free extension point
	// // Point p=findFreeSquare();
	// // p=findEdge(p.x,p.y,dx,dy);
	// Point p = m.findEdgeSquare(dx, dy, 0);
	// if (p == null)
	// continue;
	// // advance onto blank square
	// p.x += dx;
	// p.y += dy;
	//
	// // choose new feature to add
	// switch (Rand.d(7)) {
	// case 1 :
	// case 2 :
	// makeThinPassage(m,p.x, p.y, dx, dy, false);
	// break;
	//
	// case 3 :
	// makeThinPassage(m,p.x, p.y, dx, dy, true);
	// break;
	//
	// case 4 :
	// case 5 :
	// case 6 :
	// Dungeon.makeOvalRoom(m,p.x, p.y, dx, dy);
	// break;
	//
	// case 7 :
	// makeCrevice(m,p.x, p.y, dx, dy);
	// break;
	//
	// /*
	// * case 8: { int tx=p.x+dx*Rand.d(2,12)+(RPG.r(5)-2)*Rand.d(5); int
	// * ty=p.y+dy*Rand.d(2,12)+(RPG.r(5)-2)*Rand.d(5); if
	// * (makePath(p.x,p.y,tx,ty)) { makeOvalRoom(tx,ty,dx,dy); } break; }
	// */
	// }
	// }
	//
	// // turn all blanks into walls
	// m.replaceTiles(0, m.wall());
	//
	// // now do some decoration
	// for (int i = 0; i < 13; i++) {
	// m.addThing(Lib.create("rock"));
	// }
	// for (int i = 0; i < 6; i++) {
	// m.addThing(Lib.create("stone"));
	// }
	// for (int i = 0; i < 10; i++) {
	// m.addThing(Dungeon.createFoe(m));
	// }
	//
	// }
	//
	// // makes a thin passage
	// // put a wall around first half of passage
	// // this should prevent early branching
	// public static boolean makeThinPassage(Map m, int x, int y, int dx, int
	// dy,
	// boolean diagonals) {
	// int len = Rand.d(2, 12);
	// int size = Rand.d(4);
	//
	// if (!m.isBlank(x + size * dy, y - size * dx, x - size * dy + len * dx, y
	// + size * dx + len * dy))
	// return false;
	//
	// m.fillArea(x + size * dy, y - size * dx, x - size * dy + len * dx / 2, y
	// + size * dx + len * dy / 2, m.wall());
	// int p = 0;
	// for (int i = 0; i <= len; i++) {
	// m.setTile(x + i * dx + p * dy, y + i * dy - p * dx, m.floor());
	// if (Rand.d(2) == 1)
	// p = Maths.middle(-size, p + Rand.r(2) - Rand.r(2), size);
	// if (!diagonals)
	// m.setTile(x + i * dx + p * dy, y + i * dy - p * dx, m.floor());
	// }
	// return true;
	// }
	//
	// // nooks and crannies
	// private static boolean makeCrevice(Map m, int x, int y, int dx, int dy) {
	// if (!m.isBlank(x + dy, y - dx, x - dy, y + dx))
	// return false;
	// m.setTile(x + dy, y - dx, m.wall());
	// m.setTile(x - dy, y + dx, m.wall());
	//
	// switch (Rand.d(10)) {
	// case 1 : // oval room continuation
	// m.setTile(x, y, m.floor());
	// Dungeon.makeOvalRoom(m,x + dx, y + dy, dx, dy);
	// break;
	//
	// case 2 : // trap!
	// m.setTile(x, y, m.floor());
	// m.addThing(Trap.createTrap(m.getLevel()), x, y);
	// if ((Rand.d(3) == 1) && (m.getTile(x + dx, y + dy) == 0)) {
	// m.setTile(x + dx, y + dy, m.floor());
	// if (Rand.d(3) == 1)
	// m.addThing(Lib.createItem(m.getLevel()), x + dx, y + dy);
	// }
	// break;
	//
	// case 3 : // recurse!
	// m.setTile(x, y, m.floor());
	// makeCrevice(m,x + dx + (Rand.r(3) - 1) * dy, y + dy
	// - (Rand.r(3) - 1) * dx, dx, dy);
	// break;
	//
	// case 4 : // secret route
	// makeSecretDoor(m, x, y);
	// if (Rand.d(2) == 1) {
	// makeCrevice(m,x + dx, y + dy, dx, dy);
	// } else {
	// Dungeon.makeOvalRoom(m,x + dx, y + dy, dx, dy);
	// }
	// break;
	//
	// default :
	// m.setTile(x, y, m.floor());
	// }
	//
	// return true;
	// }
	//
	// private static void makeSecretDoor(Map m, int x, int y) {
	// m.setTile(x, y, m.wall());
	// m.addThing(Lib.create("secret door"), x, y);
	// }
	//
	// // make a random twisty tunnel
	// public static boolean makeTunnel(Map m,int x, int y, int dx, int dy) {
	// if (m.getTile(x, y) == 0) {
	// int ndx = dx;
	// int ndy = dy;
	// m.setTile(x, y, m.floor());
	// if (Rand.d(3) == 1) {
	// ndx = -dy;
	// ndy = dx;
	// }
	// if (Rand.d(4) == 1) {
	// ndx = dy;
	// ndy = -dx;
	// }
	// makeTunnel(m, x + ndx, y + ndy, ndx, ndy);
	// return true;
	// }
	// return false;
	// }
}