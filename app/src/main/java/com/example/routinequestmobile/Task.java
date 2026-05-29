package com.example.routinequestmobile;

public class Task {
    private Long id;
    private String name;
    private String description;
    private int xpReward;
    private String attributeType;
    private int attributePoints;
    private int durationMinutes;
    private String status;
    private String startTime;
    private boolean ativo;

    // Construtor vazio (obrigatório para o Retrofit/GSON funcionar)
    public Task() {
    }

    // Construtor completo
    public Task(String name, String description, int xpReward, String attributeType, int attributePoints, int durationMinutes) {
        this.name = name;
        this.description = description;
        this.xpReward = xpReward;
        this.attributeType = attributeType;
        this.attributePoints = attributePoints;
        this.durationMinutes = durationMinutes;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public String getAttributeType() { return attributeType; }
    public void setAttributeType(String attributeType) { this.attributeType = attributeType; }

    public int getAttributePoints() { return attributePoints; }
    public void setAttributePoints(int attributePoints) { this.attributePoints = attributePoints; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getStatus() { return status; }

    public String getStartTime() { return startTime; }

    public void setStartTime(String startTime) { this.startTime = startTime; }

    public boolean isAtivo() { return ativo; }

    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public void setStatus(String status) { this.status = status; }
}