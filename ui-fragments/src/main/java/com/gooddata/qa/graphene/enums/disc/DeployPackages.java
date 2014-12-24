package com.gooddata.qa.graphene.enums.disc;

import java.util.Arrays;
import java.util.List;

public enum DeployPackages {

    BASIC("Basic", "Basic", ProcessTypes.GRAPH, Executables.FAILED_GRAPH, Executables.LONG_TIME_RUNNING_GRAPH, Executables.SUCCESSFUL_GRAPH),
    CLOUDCONNECT("cloudconnect", "DWHS", ProcessTypes.GRAPH, Executables.DWHS1, Executables.DWHS2),
    RUBY("ruby", "ruby", ProcessTypes.RUBY, Executables.RUBY1, Executables.RUBY2),
    EXECUTABLES_GRAPH("executables", "executables", ProcessTypes.GRAPH, Executables.EXECUTABLES_GRAPH1, Executables.EXECUTABLES_GRAPH2,
            Executables.EXECUTABLES_GRAPH3, Executables.EXECUTABLES_GRAPH4),
    EXECUTABLES_RUBY("executables", "executables", ProcessTypes.RUBY, Executables.EXECUTABLES_RUBY1, Executables.EXECUTABLES_RUBY2),
    CTL_EVENT("CTL_event", "CTL_event", ProcessTypes.GRAPH, Executables.CTL_GRAPH),
    NOT_EXECUTABLE("not-executables");

    private String packageName;
    private String rootFolder;
    private ProcessTypes packageType;
    private List<Executables> executableList;

    private DeployPackages(String packageName, String rootFolder, ProcessTypes packageType,
            Executables... executables) {
        this.packageName = packageName;
        this.rootFolder = rootFolder;
        this.packageType = packageType;
        this.executableList = Arrays.asList(executables);
    }

    private DeployPackages(String packageName) {
        this(packageName, null, null);
    }

    public String getPackageName() {
        return this.packageName + ".zip";
    }

    public List<Executables> getExecutables() {
        return this.executableList;
    }

    public String getPackageRootFolder() {
        return this.rootFolder;
    }


    public ProcessTypes getPackageType() {
        return packageType;
    }

    public enum Executables {
        SUCCESSFUL_GRAPH("successfulGraph.grf", "graph"),
        FAILED_GRAPH("errorGraph.grf", "graph", 
                "Graph=Basic/graph/errorGraph.grf error: Graph=schedule_param transformation failed!: "
                + "Component [GD Dataset Writer:GD_DATASET_WRITER] finished with status ERROR.: "
                + "Unrecoverable error in SLI upload occurred."),
        LONG_TIME_RUNNING_GRAPH("longTimeRunningGraph.grf", "graph"),
        DWHS1("DWHS1.grf", "graph"),
        DWHS2("DWHS2.grf", "graph"),
        RUBY1("ruby1.rb", "script"),
        RUBY2("ruby2.rb", "script"),
        EXECUTABLES_GRAPH1("01 - REST_GET_1.grf", "graph"),
        EXECUTABLES_GRAPH2("02 - REST_POST.grf", "graph"),
        EXECUTABLES_GRAPH3("03 - REST_PUT.grf", "graph"),
        EXECUTABLES_GRAPH4("04 - REST_DELETE.grf", "graph"),
        EXECUTABLES_RUBY1("ruby1.rb", "script"),
        EXECUTABLES_RUBY2("ruby2.rb", "script"),
        CTL_GRAPH("CTL_Function.grf", "graph");

        private String executable;
        private String executableFolder;
        private String errorMessage;

        private Executables(String executable, String executableFolder, String errorMessage) {
            this.executable = executable;
            this.executableFolder = executableFolder;
            this.errorMessage = errorMessage;
        }
        
        private Executables(String executable, String executableFolder) {
            this(executable, executableFolder, null);
        }

        public String getExecutablePath() {
            return "/" + executableFolder + "/" + executable;
        }

        public String getExecutableName() {
            return this.executable;
        }
        
        public String getErrorMessage() {
            if (this.errorMessage == null)
                throw new UnsupportedOperationException("This executable doesn't have error message!");
            return this.errorMessage;
        }
    }
}
