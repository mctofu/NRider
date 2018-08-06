package nrider.interpreter;

public interface ICommand {
    String getName();

    String getDescription();

    String execute(String[] args);
}
