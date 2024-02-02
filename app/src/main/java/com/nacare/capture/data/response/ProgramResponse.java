package com.nacare.capture.data.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProgramResponse {
    @SerializedName("pager")
    private Pager pager;

    @SerializedName("programs")
    private List<Program> programs;

    public Pager getPager() {
        return pager;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public static class Pager {
        @SerializedName("page")
        private int page;

        @SerializedName("total")
        private int total;

        @SerializedName("pageSize")
        private int pageSize;

        @SerializedName("pageCount")
        private int pageCount;

        // Getter methods
    }

    public static class Program {
        @SerializedName("name")
        private String name;

        @SerializedName("id")
        private String id;

        @SerializedName("programStages")
        private List<ProgramStage> programStages;

        @SerializedName("programSections")
        private List<ProgramSections> programSections;

        // Getter methods
    }

    public static class ProgramSections {

        @SerializedName("name")
        private String name;

        @SerializedName("trackedEntityAttributes")
        private List<DataElement> dataElements;

    }



    public static class ProgramStage {
        @SerializedName("name")
        private String name;

        @SerializedName("id")
        private String id;

        @SerializedName("programStageSections")
        private List<ProgramStageSection> programStageSections;

        // Getter methods
    }

    public static class ProgramStageSection {
        @SerializedName("dataElements")
        private List<DataElement> dataElements;

        // Getter methods
    }

    public static class DataElement {
        @SerializedName("id")
        private String id;

        @SerializedName("attributeValues")
        private List<AttributeValue> attributeValues;

        // Getter methods
    }

    public static class AttributeValue {
        @SerializedName("attribute")
        public Attribute attribute;

        @SerializedName("value")
        public String value;

        // Getter methods
    }

    public static class Attribute {
        @SerializedName("name")
        public String name;

        @SerializedName("id")
        public String id;

        // Getter methods
    }
}
