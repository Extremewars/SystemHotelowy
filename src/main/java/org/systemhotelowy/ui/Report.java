package org.systemhotelowy.ui;

public class Report {
    private String title;
    private String content;
    private String status; // "Nowe", "Przeczytane"

    public Report(String title, String content, String status) {
        this.title = title;
        this.content = content;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
