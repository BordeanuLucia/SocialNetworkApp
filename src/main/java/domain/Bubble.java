package domain;


import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

import java.time.LocalDateTime;

import static utils.Constants.DATE_TIME_FORMATTER_WITH_HOUR;


public class Bubble extends Node {
    public static GridPane createBubble(String mesaj, LocalDateTime date, Boolean primit) {
        GridPane pane = new GridPane();
        Boolean receivedMessage = primit;
        SVGPath triangle = new SVGPath();
        triangle.setFill(Paint.valueOf("#6a6e6e"));
        Label labelMesaj = new Label(mesaj);
        labelMesaj.setWrapText(true);
        labelMesaj.setMaxWidth(300d);
        labelMesaj.setFont(new Font(17));
        Label labelDate = new Label(date.format(DATE_TIME_FORMATTER_WITH_HOUR));
        labelMesaj.setStyle("-fx-background-color: #6a6e6e;");

        FlowPane flow1 = new FlowPane();
        flow1.setMaxWidth(50);
        FlowPane flow2 = new FlowPane();
        flow2.setMaxWidth(300);

        if(receivedMessage) {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(5);
            ColumnConstraints col2 = new ColumnConstraints();
            col1.setPercentWidth(25);
            pane.getColumnConstraints().addAll(col2,col1);
            triangle.setContent("M0 0 L16 0 L16 16 Z");
            flow1.setAlignment(Pos.TOP_RIGHT);
            flow1.getChildren().add(triangle);
            flow2.setAlignment(Pos.TOP_LEFT);
            flow2.getChildren().add(labelMesaj);
            pane.add(flow1,0,0);
            pane.add(flow2,1,0);
        }
        else{
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(5);
            ColumnConstraints col2 = new ColumnConstraints();
            col1.setPercentWidth(25);
            pane.getColumnConstraints().addAll(col1,col2);
            triangle.setContent("M15 0 L0 15 L0 0 Z");
            flow1.setAlignment(Pos.TOP_LEFT);
            flow1.getChildren().add(triangle);
            flow2.setAlignment(Pos.TOP_RIGHT);
            flow2.getChildren().add(labelMesaj);
            pane.add(flow2,0,0);
            pane.add(flow1,1,0);
        }

        return pane;
    }


}