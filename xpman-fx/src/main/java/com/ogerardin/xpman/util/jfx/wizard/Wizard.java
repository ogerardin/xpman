package com.ogerardin.xpman.util.jfx.wizard;

import javafx.fxml.FXMLLoader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

@Slf4j
public class Wizard extends org.controlsfx.dialog.Wizard {

    public Wizard(String title) {
        setTitle(title);
    }

    /**
     * Set this Wizard's flow as a {@link org.controlsfx.dialog.Wizard.LinearFlow} of {@link WizardPane}s
     * instantiated from the specified FXML resources.
     *
     * @param pages list of FXML resources; each of them must define a WizardPane (or subclass)
     */
    protected void setFlow(String... pages) {
        WizardPane[] wizardPanes = Stream.of(pages)
                .map(this::loadWizardPane)
                .toArray(WizardPane[]::new);
        final LinearFlow flow = new LinearFlow(wizardPanes);
        setFlow(flow);
    }

    @SneakyThrows
    private WizardPane loadWizardPane(String page) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Wizard.class.getResource(page));
        loader.setControllerFactory(this::buildController);
        WizardPane wizardPane = loader.load();

        Object controller = loader.getController();
        if (wizardPane instanceof ValidatingWizardPane) {
            ValidatingWizardPane validatingWizardPane = (ValidatingWizardPane) wizardPane;
            if (controller instanceof Validating) {
                Validating validatingController = (Validating) controller;
                validatingWizardPane.setInvalidProperty(validatingController.invalidProperty());
            }
            if (controller instanceof PageListener) {
                PageListener pageListenerController = (PageListener) controller;
                validatingWizardPane.setPageListener(pageListenerController);
            }
        }
        return wizardPane;
    }

    @SneakyThrows
    private <C> C buildController(Class<C> type) {
        try {
            // if the controller class has a constructor that takes a Wizard parameter, use it
            Constructor<C> constructor = type.getConstructor(this.getClass());
            return constructor.newInstance(this);
        } catch (NoSuchMethodException e) {
            // otherwise use no-arg constructor
            return type.newInstance();
        }
    }

}
