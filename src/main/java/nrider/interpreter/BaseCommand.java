package nrider.interpreter;

public abstract class BaseCommand implements ICommand {
    public String getName() {
        String name = this.getClass().getSimpleName();
        return name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
    }

    public abstract String run(String[] args) throws Exception;

    public String execute(String[] args) {
        try {
            return run(args);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
