package es.informax.postgit;

import static es.informax.postgit.PostgitPanel.getServerUrl;
import es.informax.postgit.red.AccionRedAddProjectMember;
import es.informax.postgit.red.AccionRedAgregarHook;
import es.informax.postgit.red.AccionRedCrearProyecto;
import es.informax.postgit.red.AccionRedLogin;
import es.informax.postgit.red.Comunicador;
import es.informax.postgit.red.EventoRed;
import es.informax.postgit.red.EventoRedListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

@ActionID(
        category = "Git",
        id = "es.informax.postgit.PostGitActionListener"
)
@ActionRegistration(
        iconBase = "es/informax/postgit/images/gitlab.png",
        displayName = "#CTL_PostGitActionListener"
)
@ActionReference(path = "Toolbars/Build", position = 500)
@Messages("CTL_PostGitActionListener=Create GitLab Project")
public final class PostGitActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new GitWizardPanel1());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("New remote git project");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            final String nombre = (String) wiz.getProperty("nombre");
            final String descripcion = (String) wiz.getProperty("descripcion");

            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Project creation has been sent."));
            final Comunicador comunicadorLogin = new Comunicador(getServerUrl() + "session", new AccionRedLogin(PostgitPanel.getUser(), PostgitPanel.getPass()), null);
            EventoRedListener eventoRedListener = new EventoRedListener() {

                @Override
                public void comunicacionCompletada(EventoRed evt) {
                    if (evt.isCorrecto()) {
                        //Logged
                        JSONObject session = (JSONObject) evt.getResultado();
                        final String token = String.valueOf(session.get("private_token"));

                        new Comunicador(getServerUrl() + "projects?private_token=" + token, new AccionRedCrearProyecto(nombre, descripcion, 10), new EventoRedListener() {
                            @Override
                            public void comunicacionCompletada(EventoRed evt) {
                                if (evt.isCorrecto()) {
                                    //Proyecto creado
                                    int idProyecto = Integer.parseInt(String.valueOf(((Map)evt.getResultado()).get("id")));
                                    
                                    try {
                                        JSONParser parser = new JSONParser();
                                        JSONObject usuarios = (JSONObject)parser.parse(NbPreferences.forModule(PostgitPanel.class).get("usuariosMarcados", "{}"));
                                        for(Object usuario : usuarios.keySet()) {
                                            if((boolean)usuarios.get(usuario)) {
                                                new Comunicador(getServerUrl() + "projects/" + idProyecto + "/members?private_token=" + token, new AccionRedAddProjectMember(Integer.parseInt(String.valueOf(usuario)), 40), new EventoRedListener() {

                                                    @Override
                                                    public void comunicacionCompletada(EventoRed evt) {
                                                        //Usuarios extra enviados
                                                        if(!evt.isCorrecto()) {
                                                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error adding users to project."));
                                                        }
                                                    }
                                                }).ejecutarAccion();
                                            }
                                        }
                                    }
                                    catch(NumberFormatException | ParseException ex) { 
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error adding users to project."));
                                    }
                                    
                                    String urlProyecto = NbPreferences.forModule(PostgitPanel.class).get("urlHook", "");
                                    if(!urlProyecto.trim().isEmpty()) {
                                        //Creando un hook
                                        new Comunicador(getServerUrl() + "projects/" + idProyecto + "/hooks?private_token=" + token, new AccionRedAgregarHook(urlProyecto), new EventoRedListener() {

                                            @Override
                                            public void comunicacionCompletada(EventoRed evt) {
                                                if(!evt.isCorrecto()) {
                                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error adding web hook to project."));
                                                }
                                            }
                                        }).ejecutarAccion();
                                    }
                                }
                                else {
                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error - Check if the project already exists and your credentials at Tools > options > Miscellaneous > Gitlab."));
                                }
                            }
                        }).ejecutarAccion();
                    }
                    else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Login error."));
                    }
                }
            };
            comunicadorLogin.setListener(eventoRedListener);
            comunicadorLogin.ejecutarAccion();
        }
    }
}
