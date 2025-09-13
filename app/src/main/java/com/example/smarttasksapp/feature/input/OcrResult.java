package com.example.smarttasksapp.feature.input;

import androidx.annotation.NonNull;
import java.util.List;

/**
 * 阿里云OCR识别结果的数据模型类
 * 用于存储OCR识别的结构化数据
 */
public class OcrResult {
    private String algo_version;
    private int angle;
    private String content;
    private int height;
    private int orgHeight;
    private int orgWidth;
    private String prism_version;
    private int prism_wnum;
    private List<WordInfo> prism_wordsInfo;
    private int width;

    // Getters and Setters
    public String getAlgo_version() {
        return algo_version;
    }

    public void setAlgo_version(String algo_version) {
        this.algo_version = algo_version;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getOrgHeight() {
        return orgHeight;
    }

    public void setOrgHeight(int orgHeight) {
        this.orgHeight = orgHeight;
    }

    public int getOrgWidth() {
        return orgWidth;
    }

    public void setOrgWidth(int orgWidth) {
        this.orgWidth = orgWidth;
    }

    public String getPrism_version() {
        return prism_version;
    }

    public void setPrism_version(String prism_version) {
        this.prism_version = prism_version;
    }

    public int getPrism_wnum() {
        return prism_wnum;
    }

    public void setPrism_wnum(int prism_wnum) {
        this.prism_wnum = prism_wnum;
    }

    public List<WordInfo> getPrism_wordsInfo() {
        return prism_wordsInfo;
    }

    public void setPrism_wordsInfo(List<WordInfo> prism_wordsInfo) {
        this.prism_wordsInfo = prism_wordsInfo;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @NonNull
    @Override
    public String toString() {
        return "OcrResult{" +
                "algo_version='" + algo_version + '\'' +
                ", angle=" + angle +
                ", content='" + content + '\'' +
                ", height=" + height +
                ", orgHeight=" + orgHeight +
                ", orgWidth=" + orgWidth +
                ", prism_version='" + prism_version + '\'' +
                ", prism_wnum=" + prism_wnum +
                ", prism_wordsInfo=" + prism_wordsInfo +
                ", width=" + width +
                '}';
    }

    /**
     * OCR识别中的单个文字信息
     */
    public static class WordInfo {
        private int angle;
        private int direction;
        private int height;
        private List<Point> pos;
        private int prob;
        private int width;
        private String word;
        private int x;
        private int y;

        // Getters and Setters
        public int getAngle() {
            return angle;
        }

        public void setAngle(int angle) {
            this.angle = angle;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public List<Point> getPos() {
            return pos;
        }

        public void setPos(List<Point> pos) {
            this.pos = pos;
        }

        public int getProb() {
            return prob;
        }

        public void setProb(int prob) {
            this.prob = prob;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @NonNull
        @Override
        public String toString() {
            return "WordInfo{" +
                    "angle=" + angle +
                    ", direction=" + direction +
                    ", height=" + height +
                    ", pos=" + pos +
                    ", prob=" + prob +
                    ", width=" + width +
                    ", word='" + word + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    /**
     * 坐标点类，用于表示文字在图片中的位置
     */
    public static class Point {
        private int x;
        private int y;

        // Getters and Setters
        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @NonNull
        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}