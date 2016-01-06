//package nars.guifx;
//
//import javafx.application.Application;
//import javafx.event.Event;
//import javafx.event.EventHandler;
//import javafx.event.EventType;
//import javafx.geometry.Point2D;
//import javafx.scene.Group;
//import javafx.scene.Node;
//import javafx.scene.Scene;
//import javafx.scene.canvas.Canvas;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.input.MouseEvent;
//import javafx.stage.Stage;
//import static javafx.scene.input.MouseEvent.*;
//
//import rx.Observable;
//import rx.Subscriber;
//import rx.subscriptions.Subscriptions;
//
//import java.util.function.Function;
//
//public class Paint extends Application {
//
//    public static void main(String[] args) {
//        javafx.application.Application.launch(Paint.class);
//    }
//
//    @Override
//    public void start(Stage stage) throws Exception {
//
//        Integer screenWidth = 800;
//        Integer screenHeight = 600;
//
//        Canvas canvas = new Canvas(screenHeight, screenWidth);
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        Group root = new Group();
//        root.getChildren().add(canvas);
//
//        Observable<Boolean> leftButtonUp = leftButtonUp(canvas);
//        Observable<Boolean> leftButtonDown = leftButtonDown(canvas);
//
//        leftButtonUp.subscribe(up -> System.out.println("up: "+up));
//        leftButtonDown.subscribe(down -> System.out.println("down: "+down));
//
//        Observable<MouseEvent> mouseMoves = mouseMoves(canvas);
//        Observable<MouseEvent>  mouseDrags = mouseDrags(canvas);
//
//        Observable<MouseEvent>  mouse = Observable.merge(mouseMoves, mouseDrags);
//
//        Observable<Point2D[]> mouseDiffs =
//                mouse
//                        .buffer(2, 1)
//                        .map(buffer -> new Point2D[]{
//                                new Point2D(buffer.get(0).getX(), buffer.get(0).getY()),
//                                new Point2D(buffer.get(1).getX(), buffer.get(1).getY())
//                        });
//
//        Observable<Point2D[]> paint =
//                mouseDiffs
//                        .window(leftButtonDown, b -> leftButtonUp)
//                        .flatMap(x -> x);
//
//        paint.subscribe(diff -> {
//            gc.strokeLine(
//                    diff[0].getX(), diff[0].getY(),
//                    diff[1].getX(), diff[1].getY()
//            );
//        });
//
//        stage.setTitle("Rx Paint");
//        stage.setScene(new Scene(root));
//        stage.show();
//    }
//
//    // eat your heart out getting co/contravariance right ;-)
//    <T> Observable<T> fromEvent(Node node, EventType<? extends Event> event, Function<? super Event, ? extends T> selector) {
//        return Observable.create((Subscriber<? super T> subscriber) -> {
//            EventHandler<? super Event> handler = e -> subscriber.onNext(selector.apply(e));
//            node.addEventHandler(event, handler);
//            subscriber.add(Subscriptions.create(() -> node.removeEventHandler(event, handler)));
//        });
//    }
//
//    Observable<Boolean> leftButtonDown(Canvas canvas) {
//        return fromEvent(canvas, MOUSE_PRESSED, event -> true);
//    }
//
//    Observable<Boolean> leftButtonUp(Canvas canvas) {
//        return fromEvent(canvas, MOUSE_RELEASED, event -> false);
//    }
//
//    Observable<MouseEvent> mouseMoves(Canvas canvas) {
//        return fromEvent(canvas, MOUSE_MOVED, event -> (MouseEvent)event);
//    }
//
//    Observable<MouseEvent> mouseDrags(Canvas canvas) {
//        return fromEvent(canvas, MOUSE_DRAGGED, event -> (MouseEvent) event);
//    }
// }