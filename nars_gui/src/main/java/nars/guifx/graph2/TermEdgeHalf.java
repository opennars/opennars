package nars.guifx.graph2;

/**
 * Created by me on 9/5/15.
 */
class TermEdgeHalf {
	//
	// private final TermEdge edge;
	// long lastUpdate = -1;
	//
	// final double minThickVisibility = 0.25;
	//
	// private final TermNode from;
	// private final TermNode to;
	//
	// float taskPri = 0, termPri = 0;
	// private int tasks;
	//
	// //SimpleDoubleProperty thickness = new SimpleDoubleProperty();
	// public Color fill;
	// protected double thickness;
	//
	// public void set(TaskLink t, long when) {
	// if (lastUpdate != when) {
	// reset(when);
	// }
	//
	// taskPri += t.getPriority();
	// tasks++;
	// }
	//
	// protected void reset(long when) {
	// //edge.changed.set(true);
	// taskPri = termPri = 0;
	// tasks = 0;
	// lastUpdate = when;
	// }
	//
	// public void set(TermLink t, long when) {
	// if (lastUpdate != when) {
	// reset(when);
	// }
	//
	// termPri += t.getPriority();
	// }
	//
	// // final protected void dirty(boolean newValue) {
	// // dirty.set(newValue);
	// // if (newValue)
	// // edgeDirty.set(true);
	// // }
	// //
	//
	//
	// public TermEdgeHalf(TermNode from, TermNode to, TermEdge termEdge) {
	// super();
	// this.from = from;
	// this.to = to;
	// this.edge = termEdge;
	//
	//
	// //setManaged(false);
	//
	// //getPoints().setAll(0.5d, 0d, -0.5d, -0.5d, -0.5d, +0.5d); //isoceles
	// triangle within -0.5,-0.5...0.5,0.5 (len/wid = 1)
	//
	// // double q = 0.25f;
	// // if (!order(from.term, to.term)) {
	// // getPoints().setAll(0.5d, 0d, -0.5d, q, -0.5d, -q); //right triangle
	// // } else {
	// // //180deg rotate
	// // getPoints().setAll(-0.5d, 0d, 0.5d, -q, 0.5d, q); //right triangle
	// // }
	//
	//
	// }
	//
	//
	// public boolean update() {
	// if (termPri > 1) termPri = 1;
	// float taskPriMean = tasks > 0 ? taskPri / tasks : 0;
	// if (taskPriMean > 1) taskPriMean = 1f;
	//
	// double t = 0.5f * (taskPriMean + termPri);
	//
	// boolean vis = (t > minThickVisibility);
	// if (!vis)
	// return false;
	//
	// double fw = from.width();
	// //double fh = from.height();
	// double tw = to.width();
	// //double th = to.height();
	// double thickness = t * Math.min(fw, tw);
	// this.thickness = thickness;
	//
	// this.fill = NARGraph1.visModel.getEdgeColor(termPri, taskPri / tasks);
	//
	// return true;
	// }
	//
	// // public final void updateIfVisible() {
	// //
	// //
	// //// int numTasks = taskLinks.size();
	// //// final double taskSum, taskMean;
	// //// if (numTasks > 0) {
	// //// this.taskPrioSum = taskSum = taskLinks.stream()
	// //// .mapToDouble(t -> t.getPriority()).sum();//.orElse(0);
	// //// taskMean = taskSum / numTasks;
	// //// } else {
	// //// taskSum = taskMean = 0;
	// //// }
	// //
	// // //temporary
	// // //float taskMean = termPri + taskPri;
	// //
	// // //final double termPrio = termLink != null ? termLink.getPriority() :
	// 0;
	// // //this.thickness = (taskMean + termPrio);
	// //
	// // //dirty(false);
	// // }
	//
	//
}
