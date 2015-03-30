/**
 * EPML2Canonical is a class for converting an TypeEPML object
 * into a CanonicalProcessType object.
 * A EPML2Canonical object encapsulates the state of the two main
 * components resulted from the canonization process.  This
 * state information includes:
 * <ul>
 * <li>CanonicalProcessType object
 * <li>AnnotationsType object
 * </ul>
 * <p>
 *
 *

 @author Abdul
 *

 @version     %I%, %G%
 *

 @since 1.0
 */
package org.apromore.canoniser.adapters;

import de.epml.TEpcElement;
import de.epml.TExtensibleElements;
import de.epml.TypeAND;
import de.epml.TypeArc;
import de.epml.TypeEPC;
import de.epml.TypeEPML;
import de.epml.TypeEvent;
import de.epml.TypeFunction;
import de.epml.TypeMove2;
import de.epml.TypeOR;
import de.epml.TypeObject;
import de.epml.TypeProcessInterface;
import de.epml.TypeRANGE;
import de.epml.TypeRole;
import de.epml.TypeXOR;
import org.apromore.anf.AnnotationsType;
import org.apromore.anf.FillType;
import org.apromore.anf.FontType;
import org.apromore.anf.GraphicsType;
import org.apromore.anf.LineType;
import org.apromore.anf.PositionType;
import org.apromore.anf.SizeType;
import org.apromore.exception.CanoniserException;
import org.apromore.cpf.ANDJoinType;
import org.apromore.cpf.ANDSplitType;
import org.apromore.cpf.CanonicalProcessType;
import org.apromore.cpf.EdgeType;
import org.apromore.cpf.EventType;
import org.apromore.cpf.HumanType;
import org.apromore.cpf.InputOutputType;
import org.apromore.cpf.NetType;
import org.apromore.cpf.NodeType;
import org.apromore.cpf.ORJoinType;
import org.apromore.cpf.ORSplitType;
import org.apromore.cpf.ObjectRefType;
import org.apromore.cpf.ObjectType;
import org.apromore.cpf.ResourceTypeRefType;
import org.apromore.cpf.TaskType;
import org.apromore.cpf.TypeAttribute;
import org.apromore.cpf.WorkType;
import org.apromore.cpf.XORJoinType;
import org.apromore.cpf.XORSplitType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EPML2Canonical {

    Map<BigInteger, String> id_map = new HashMap<BigInteger, String>();
    List<String> flow_source_id_list = new LinkedList<String>();
    List<TypeAND> and_list = new LinkedList<TypeAND>();
    List<TypeOR> or_list = new LinkedList<TypeOR>();
    List<TypeXOR> xor_list = new LinkedList<TypeXOR>();
    Map<BigInteger, String> def_ref = new HashMap<BigInteger, String>();
    Map<BigInteger, TypeRole> role_ref = new HashMap<BigInteger, TypeRole>();
    Map<BigInteger, TypeObject> obj_ref = new HashMap<BigInteger, TypeObject>();
    List<TaskType> subnet_list = new LinkedList<TaskType>();
    List<BigInteger> range_ids = new LinkedList<BigInteger>();
    List<String> event_ids = new LinkedList<String>();
    List<TypeArc> range_flow = new LinkedList<TypeArc>();
    List<TypeArc> range_relation = new LinkedList<TypeArc>();

    private CanonicalProcessType cproc = new CanonicalProcessType();
    private AnnotationsType annotations = new AnnotationsType();
    private long ids = System.currentTimeMillis();


    public CanonicalProcessType getCPF() {
        return cproc;
    }

    public AnnotationsType getANF() {
        return annotations;
    }

    /**
     * The constructor receives the header then does the canonization process
     * in order to allow the user to retrieve the produced process again into
     * the canonical format. The user also will be able to retrieve the annotation
     * element which stores the annotation data for the canonized modelass isolated
     * from the process flow.
     * <p/>
     *
     * @param epml the header for an EPML (EPC Markup Language) which
     *             is file format for EPC diagrams.
     * @throws org.apromore.exception.CanoniserException
     *
     * @since 1.0
     */
    public EPML2Canonical(TypeEPML epml) throws CanoniserException {
        main(epml);
    }

    public EPML2Canonical(TypeEPML epml, long id) throws CanoniserException {
        this.ids = id;
        main(epml);
    }

    void main(TypeEPML epml) throws CanoniserException {
        epml = removeFakes(epml);

        TypeAttribute att = new TypeAttribute();
        att.setTypeRef("IntialFormat");
        att.setValue("EPML");
        cproc.getAttribute().add(att);

        if (epml.getDirectory() != null && epml.getDirectory().size() > 0) {
            for (int i = 0; i < epml.getDirectory().size(); i++) {
                for (TExtensibleElements epc : epml.getDirectory().get(i).getEpcOrDirectory()) {
                    if (epc instanceof TypeEPC) {
                        NetType net = new NetType();
                        translateEpc(net, (TypeEPC) epc);
                        id_map.put(((TypeEPC) epc).getEpcId(), String.valueOf(ids));
                        net.setId(String.valueOf(ids++));
                        cproc.getNet().add(net);
                    }
                }
                for (TaskType task : subnet_list)
                    task.setSubnetId(id_map.get(task.getSubnetId()));
                subnet_list.clear();
            }
        } else {
            // the epml element doesn't have any directory
            for (TypeEPC epc : epml.getEpcs()) {
                NetType net = new NetType();
                translateEpc(net, epc);
                id_map.put(epc.getEpcId(), String.valueOf(ids));
                net.setId(String.valueOf(ids++));
                cproc.getNet().add(net);
            }
            for (TaskType task : subnet_list)
                task.setSubnetId(id_map.get(task.getSubnetId()));
            subnet_list.clear();
        }
    }

    /**
     * This method for removing the fake functions
     * and events in case the modelass has them.
     * <p/>
     *
     * @param epml the header for an EPML
     * @return epml      the header for the EPML modelass after modification
     * @since 1.0
     */
    private TypeEPML removeFakes(TypeEPML epml) {
        List<TEpcElement> remove_list = new LinkedList<TEpcElement>();
        List<TypeArc> arc_remove_list = new LinkedList<TypeArc>();
        boolean dir = true;

        if (epml.getDirectory() != null && epml.getDirectory().size() > 0) {
            for (int i = 0; i < epml.getDirectory().size(); i++) {
                for (TExtensibleElements epc : epml.getDirectory().get(i).getEpcOrDirectory()) {
                    if (epc instanceof TypeEPC) {
                        for (Object element : ((TypeEPC) epc).getEventOrFunctionOrRole()) {
                            if (element instanceof TypeFunction || element instanceof TypeEvent) {
                                QName typeRef = new QName("typeRef");
                                String str = ((TEpcElement) element).getOtherAttributes().get(typeRef);
                                if (str != null && str.equals("fake")) {
                                    remove_list.add((TEpcElement) element);
                                }
                            }
                        }
                        for (TEpcElement element : remove_list) {
                            for (Object arc : ((TypeEPC) epc).getEventOrFunctionOrRole()) {
                                if (arc instanceof TypeArc) {
                                    if (((TypeArc) arc).getFlow() != null)
                                        if (((TypeArc) arc).getFlow().getSource().equals(element.getId())) {
                                            for (Object arc2 : ((TypeEPC) epc).getEventOrFunctionOrRole()) {
                                                if (arc2 instanceof TypeArc)
                                                    if (((TypeArc) arc2).getFlow().getTarget().equals(element.getId()))
                                                        ((TypeArc) arc2).getFlow().setTarget(((TypeArc) arc).getFlow().getTarget());
                                            }
                                            arc_remove_list.add((TypeArc) arc);
                                        }
                                }
                            }
                            for (TypeArc arc : arc_remove_list)
                                ((TypeEPC) epc).getEventOrFunctionOrRole().remove(arc);
                            ((TypeEPC) epc).getEventOrFunctionOrRole().remove(element);
                        }
                    }
                }
            }
        } else {
            // the epml element doesn't have any directory
            dir = false;
            for (TypeEPC epc : epml.getEpcs()) {
                for (Object element : ((TypeEPC) epc).getEventOrFunctionOrRole()) {
                    if (element instanceof TypeFunction || element instanceof TypeEvent) {
                        QName typeRef = new QName("typeRef");
                        if (((TEpcElement) element).getOtherAttributes().get(typeRef) == "fake") {
                            remove_list.add((TEpcElement) element);
                        }
                    }
                }
                for (TEpcElement element : remove_list) {
                    for (Object arc : ((TypeEPC) epc).getEventOrFunctionOrRole()) {
                        if (arc instanceof TypeArc) {
                            if (((TypeArc) arc).getFlow() != null)
                                if (((TypeArc) arc).getFlow().getSource().equals(element.getId())) {
                                    for (Object arc2 : ((TypeEPC) epc).getEventOrFunctionOrRole()) {
                                        if (arc2 instanceof TypeArc)
                                            if (((TypeArc) arc2).getFlow().getTarget().equals(element.getId()))
                                                ((TypeArc) arc2).getFlow().setTarget(((TypeArc) arc).getFlow().getTarget());
                                    }
                                    arc_remove_list.add((TypeArc) arc);
                                }
                        }
                    }
                    for (TypeArc arc : arc_remove_list)
                        ((TypeEPC) epc).getEventOrFunctionOrRole().remove(arc);
                    ((TypeEPC) epc).getEventOrFunctionOrRole().remove(element);
                }
            }
        }

        return epml;
    }

    @SuppressWarnings("unchecked")
    private void translateEpc(NetType net, TypeEPC epc) throws CanoniserException {
        Map<String, String> role_names = new HashMap<String, String>();

        for (Object obj : epc.getEventOrFunctionOrRole()) {
            if (obj instanceof JAXBElement) {
                JAXBElement element = (JAXBElement) obj;
                if (element.getValue() instanceof TypeEvent) {
                    translateEvent(net, (TypeEvent) element.getValue());
                    addNodeAnnotations(element.getValue());

                } else if (element.getValue() instanceof TypeFunction) {
                    translateFunction(net, ((JAXBElement<TypeFunction>)obj).getValue());
                    addNodeAnnotations(element.getValue());

                } else if (element.getValue() instanceof TypeAND) {
                    id_map.put(((TEpcElement) element.getValue()).getId(), String.valueOf(ids));
                    addNodeAnnotations(element.getValue());
                    ((TEpcElement) element.getValue()).setId(BigInteger.valueOf(ids++));
                    and_list.add((TypeAND) element.getValue());

                } else if (element.getValue() instanceof TypeOR) {
                    id_map.put(((TEpcElement) element.getValue()).getId(), String.valueOf(ids));
                    addNodeAnnotations(element.getValue());
                    ((TEpcElement) element.getValue()).setId(BigInteger.valueOf(ids++));
                    or_list.add((TypeOR) element.getValue());

                } else if (element.getValue() instanceof TypeXOR) {
                    id_map.put(((TEpcElement) element.getValue()).getId(), String.valueOf(ids));
                    addNodeAnnotations(element.getValue());
                    ((TEpcElement) element.getValue()).setId(BigInteger.valueOf(ids++));
                    xor_list.add((TypeXOR) element.getValue());

                } else if (element.getValue() instanceof TypeRole) {
                    if (!role_names.containsKey(((TypeRole) element.getValue()).getName())) {
                        translateRole((TypeRole) element.getValue());
                        addNodeAnnotations(element.getValue());
                        role_names.put(((TypeRole) element.getValue()).getName(), String.valueOf(ids - 1));
                    } else {
                        id_map.put(((TypeRole) element.getValue()).getId(), role_names.get(((TypeRole) element.getValue()).getName()));
                    }

                } else if (element.getValue() instanceof TypeObject) {
                    translateObject((TypeObject) element.getValue());
                    addNodeAnnotations(element.getValue());

                } else if (element.getValue() instanceof TypeRANGE) {
                    range_ids.add(((TypeRANGE) element.getValue()).getId());

                } else if (element.getValue() instanceof TypeProcessInterface) {
                    translatePI(net, (TypeProcessInterface) element.getValue());
                    addNodeAnnotations(element.getValue());

                } else if (element.getValue() instanceof TypeArc) {
                    TypeArc arc = (TypeArc) element.getValue();
                    if (arc.getFlow() != null) {
                        if (range_ids.contains(arc.getFlow().getSource()) || range_ids.contains(arc.getFlow().getTarget())) {
                            range_flow.add(arc);
                        } else {
                            translateArc(net, arc);
                        }
                        addEdgeAnnotation(arc);
                    } else if (arc.getRelation() != null) {
                        if (range_ids.contains(arc.getRelation().getSource()) || range_ids.contains(arc.getRelation().getTarget())) {
                            range_relation.add(arc);
                        } else {
                            translateArc(net, arc);
                        }
                        addEdgeAnnotation(arc);
                    }
                }
            }
        }

        for (TypeArc arc : range_flow) {
            if (range_ids.contains(arc.getFlow().getSource())) {
                for (TypeArc arc2 : range_flow) {
                    if (range_ids.contains(arc2.getFlow().getTarget())) {
                        arc.getFlow().setSource(arc2.getFlow().getSource());
                        translateArc(net, arc);
                        addEdgeAnnotation(arc);
                    }
                }
            }
        }

        for (TypeArc arc : range_relation) {
            if (range_ids.contains(arc.getRelation().getSource())) {
                for (TypeArc arc2 : range_relation) {
                    if (range_ids.contains(arc2.getRelation().getTarget())) {
                        arc.getRelation().setSource(arc2.getRelation().getSource());
                        translateArc(net, arc);
                        addEdgeAnnotation(arc);
                    }
                }
            }
        }

        //process the gateway lists
        int counter;
        for (TypeAND and : and_list) {
            counter = 0;
            BigInteger n = and.getId();
            for (String s : flow_source_id_list)
                if (n.equals(s))
                    counter++;
            if (counter <= 1)
            //TODO
            //the and is joint
            {
                ANDJoinType andJ = new ANDJoinType();
                andJ.setId(String.valueOf(and.getId()));
                andJ.setName(and.getName());
                net.getNode().add(andJ);
            } else
            //TODO
            //the and is split, create it
            {
                ANDSplitType andS = new ANDSplitType();
                andS.setId(String.valueOf(and.getId()));
                andS.setName(and.getName());
                net.getNode().add(andS);
            }
        }
        and_list.clear();

        /// make the same for or
        for (TypeOR or : or_list) {
            counter = 0;
            BigInteger n = or.getId();
            for (String s : flow_source_id_list)
                if (n.equals(s))
                    counter++;
            if (counter <= 1)
            //TODO
            //the or is joint
            {
                ORJoinType orJ = new ORJoinType();
                orJ.setId(String.valueOf(or.getId()));
                orJ.setName(or.getName());
                net.getNode().add(orJ);
            } else
            //TODO
            //or is split, create it then remove the events after
            {
                ORSplitType orS = new ORSplitType();
                orS.setId(String.valueOf(or.getId()));
                orS.setName(or.getName());
                net.getNode().add(orS);
                processUnrequiredEvents(net, or.getId()); // after creating the split node ,, delete the event
            }
        }
        or_list.clear();

        // make the same for xor
        for (TypeXOR xor : xor_list) {
            counter = 0;
            BigInteger n = xor.getId();
            for (String s : flow_source_id_list)
                if (n.equals(s))
                    counter++;
            if (counter <= 1)
            //TODO
            // xor is joint
            {
                XORJoinType xorJ = new XORJoinType();
                xorJ.setId(String.valueOf(xor.getId()));
                xorJ.setName(xor.getName());
                net.getNode().add(xorJ);
            } else
            //xor is split, create it
            {
                XORSplitType xorS = new XORSplitType();
                xorS.setId(String.valueOf(xor.getId()));
                xorS.setName(xor.getName());
                net.getNode().add(xorS);
                processUnrequiredEvents(net, xor.getId()); // after creating the split node ,, delete the event
            }
        }
        xor_list.clear();

        // find the edge after the split
        // and remove the event
        //TODO

    }


    private void addEdgeAnnotation(TypeArc arc) throws CanoniserException {

        LineType line = new LineType();
        GraphicsType graph = new GraphicsType();
        FontType font = new FontType();

        if (arc.getGraphics() != null) {
            graph.setCpfId(id_map.get(arc.getId()));
            if (arc.getGraphics().size() > 0) {
                if (arc.getGraphics().get(0) != null) {
                    if (arc.getGraphics().get(0).getFont() != null) {
                        font.setColor(arc.getGraphics().get(0).getFont().getColor());
                        font.setDecoration(arc.getGraphics().get(0).getFont().getDecoration());
                        font.setFamily(arc.getGraphics().get(0).getFont().getFamily());
                        font.setHorizontalAlign(arc.getGraphics().get(0).getFont().getHorizontalAlign());
                        font.setRotation(arc.getGraphics().get(0).getFont().getRotation());
                        font.setSize(arc.getGraphics().get(0).getFont().getSize());
                        font.setStyle(arc.getGraphics().get(0).getFont().getStyle());
                        font.setVerticalAlign(arc.getGraphics().get(0).getFont().getVerticalAlign());
                        font.setWeight(arc.getGraphics().get(0).getFont().getWeight());
                        graph.setFont(font);
                    }
                    if (arc.getGraphics().get(0).getLine() != null) {
                        line.setColor(arc.getGraphics().get(0).getLine().getColor());
                        line.setShape(arc.getGraphics().get(0).getLine().getShape());
                        line.setStyle(arc.getGraphics().get(0).getLine().getStyle());
                        line.setWidth(arc.getGraphics().get(0).getLine().getWidth());
                        graph.setLine(line);
                    }

                    for (TypeMove2 mov2 : arc.getGraphics().get(0).getPosition()) {
                        PositionType pos = new PositionType();
                        pos.setX(mov2.getX());
                        pos.setY(mov2.getY());
                        graph.getPosition().add(pos);
                    }
                    annotations.getAnnotation().add(graph);
                }
            }
        }
    }

    private void addNodeAnnotations(Object obj) {
        GraphicsType graphT = new GraphicsType();
        LineType line = new LineType();
        FillType fill = new FillType();
        PositionType pos = new PositionType();
        SizeType size = new SizeType();
        FontType font = new FontType();
        String cpfId;

        //

        TEpcElement element = (TEpcElement) obj;
        cpfId = id_map.get(element.getId());

        if (element.getGraphics() != null) {
            if (element.getGraphics().getFill() != null) {
                fill.setColor(element.getGraphics().getFill().getColor());
                fill.setGradientColor(element.getGraphics().getFill().getGradientColor());
                fill.setGradientRotation(element.getGraphics().getFill().getGradientRotation());
                fill.setImage(element.getGraphics().getFill().getImage());
                graphT.setFill(fill);

            }

            if (element.getGraphics().getPosition() != null) {
                size.setHeight(element.getGraphics().getPosition().getHeight());
                size.setWidth(element.getGraphics().getPosition().getWidth());
                graphT.setSize(size);

                pos.setX(element.getGraphics().getPosition().getX());
                pos.setY(element.getGraphics().getPosition().getY());
                graphT.getPosition().add(pos);
            }

            if (element.getGraphics().getLine() != null) {
                line.setColor(element.getGraphics().getLine().getColor());
                line.setShape(element.getGraphics().getLine().getShape());
                line.setStyle(element.getGraphics().getLine().getStyle());
                line.setWidth(element.getGraphics().getLine().getWidth());
                graphT.setLine(line);
            }

            if (element.getGraphics().getFont() != null) {
                font.setColor(element.getGraphics().getFont().getColor());
                font.setDecoration(element.getGraphics().getFont().getDecoration());
                font.setFamily(element.getGraphics().getFont().getFamily());
                font.setHorizontalAlign(element.getGraphics().getFont().getHorizontalAlign());
                font.setRotation(element.getGraphics().getFont().getRotation());
                font.setSize(element.getGraphics().getFont().getSize());
                font.setStyle(element.getGraphics().getFont().getStyle());
                font.setVerticalAlign(element.getGraphics().getFont().getVerticalAlign());
                font.setWeight(element.getGraphics().getFont().getWeight());
                graphT.setFont(font);
            }

            graphT.setCpfId(cpfId);
            annotations.getAnnotation().add(graphT);
        }
    }

    // should be in the end

    private void processUnrequiredEvents(NetType net, BigInteger id) throws CanoniserException {
        List<EdgeType> edge_remove_list = new LinkedList<EdgeType>();
        List<NodeType> node_remove_list = new LinkedList<NodeType>();
        String event_id;
        boolean found = false;
        for (EdgeType edge : net.getEdge()) {
            if (edge.getSourceId() != null) {
                if (edge.getSourceId().equals(id) && event_ids.contains(edge.getTargetId())) {
                    event_id = edge.getTargetId();
                    for (EdgeType edge2 : net.getEdge())
                        if (edge2.getSourceId() != null && edge2.getSourceId().equals(event_id)) {
                            edge.setTargetId(edge2.getTargetId());
                            edge_remove_list.add(edge2);
                            found = true; // To know in case the event is the last element in the mode
                        }
                    // delete the unrequired event and set its name as a condition for the edge
                    for (NodeType node : net.getNode())
                        if (node.getId().equals(event_id)) {
                            if (found) {
                                edge.setCondition(node.getName());
                                node_remove_list.add(node);
                            } else {
                                edge.setCondition(node.getName());
                                node.setName("");
                            }
                        }
                }
            }
        }

        for (EdgeType edge : edge_remove_list)
            net.getEdge().remove(edge);
        edge_remove_list.clear();
        for (NodeType node : node_remove_list)
            net.getNode().remove(node);
        node_remove_list.clear();

    }

    private void translateEvent(NetType net, TypeEvent event) {
        EventType node = new EventType();
        id_map.put(event.getId(), String.valueOf(ids));
        event_ids.add(String.valueOf(ids));
        node.setId(String.valueOf(ids++));
        node.setName(event.getName());
        net.getNode().add(node);
    }

    private void translateFunction(NetType net, TypeFunction func) {
        TaskType task = new TaskType();
        id_map.put(func.getId(), String.valueOf(ids));
        task.setId(String.valueOf(ids++));
        task.setName(func.getName());
        if (func.getToProcess() != null) {
            if (func.getToProcess().getLinkToEpcId() != null) {
                task.setSubnetId(String.valueOf(func.getToProcess().getLinkToEpcId()));
                subnet_list.add(task);
            }
        }
        net.getNode().add(task);
    }

    private void translatePI(NetType net, TypeProcessInterface pi) {
        TaskType task = new TaskType();
        id_map.put(pi.getId(), String.valueOf(ids));
        task.setId(String.valueOf(ids++));
        task.setSubnetId(String.valueOf(pi.getToProcess().getLinkToEpcId())); // Will be modified later to the ID for Net
        subnet_list.add(task);
        net.getNode().add(task);
    }

    private void translateArc(NetType net, TypeArc arc) {
        if (arc.getFlow() != null
                && id_map.get(arc.getFlow().getSource()) != null
                && id_map.get(arc.getFlow().getTarget()) != null) // if it is null, that's mean the arc is relation
        {
            EdgeType edge = new EdgeType();
            id_map.put(arc.getId(), String.valueOf(ids));
            edge.setId(String.valueOf(ids++));
            edge.setSourceId(id_map.get(arc.getFlow().getSource()));
            edge.setTargetId(id_map.get(arc.getFlow().getTarget()));
            net.getEdge().add(edge);
            flow_source_id_list.add(edge.getSourceId());
        } else if (arc.getRelation() != null) {
            for (NodeType node : net.getNode()) {
                if (node.getId().equals(id_map.get(arc.getRelation().getSource()))) {
                    if (arc.getRelation().getType() != null && arc.getRelation().getType().equals("role")) {
                        ResourceTypeRefType ref = new ResourceTypeRefType();
                        TypeAttribute att = new TypeAttribute();
                        id_map.put(arc.getId(), String.valueOf(ids));
                        att.setTypeRef("RefID");
                        att.setValue(String.valueOf(ids++));
                        ref.getAttribute().add(att);
                        ref.setResourceTypeId(id_map.get(arc.getRelation().getTarget()));
                        if (role_ref.get(arc.getRelation().getSource()) != null) {
                            ref.setOptional(role_ref.get(arc.getRelation().getSource()).isOptional());
                            ref.setQualifier(role_ref.get(arc.getRelation().getSource()).getDescription()); /// update
                        }
                        ((WorkType) node).getResourceTypeRef().add(ref);
                    } else {
                        ObjectRefType ref = new ObjectRefType();
                        TypeAttribute att = new TypeAttribute();
                        id_map.put(arc.getId(), String.valueOf(ids));
                        att.setTypeRef("RefID");
                        att.setValue(String.valueOf(ids++));
                        ref.getAttribute().add(att);
                        ref.setObjectId(id_map.get(arc.getRelation().getTarget()));
                        ref.setType(InputOutputType.OUTPUT);
                        if (obj_ref.get(arc.getRelation().getTarget()) != null) {
                            ref.setOptional(obj_ref.get(arc.getRelation().getTarget()).isOptional());
                            ref.setConsumed(obj_ref.get(arc.getRelation().getTarget()).isConsumed());
                        }
                        ((WorkType) node).getObjectRef().add(ref);
                    }
                } else if (node.getId().equals(id_map.get(arc.getRelation().getTarget()))) {

                    ObjectRefType ref = new ObjectRefType();
                    TypeAttribute att = new TypeAttribute();
                    id_map.put(arc.getId(), String.valueOf(ids));
                    att.setTypeRef("RefID");
                    att.setValue(String.valueOf(ids++));
                    ref.getAttribute().add(att);
                    ref.setObjectId(id_map.get(arc.getRelation().getSource()));
                    ref.setType(InputOutputType.INPUT);
                    // TODO fixing the null cause , missing sources an targets
                    //ref.setOptional(obj_ref.get(arc.getRelation().getSource()).isOptional());
                    //ref.setConsumed(obj_ref.get(arc.getRelation().getSource()).isConsumed());
                    ((WorkType) node).getObjectRef().add(ref);

                }
            }
        }
    }

    private void translateGateway(NetType net, Object object) {
        id_map.put(((TEpcElement) object).getId(), String.valueOf(ids));
        ((TEpcElement) object).setId(BigInteger.valueOf(ids++));

        if (object instanceof TypeAND) {
            and_list.add((TypeAND) object);
        } else if (object instanceof TypeOR) {
            or_list.add((TypeOR) object);
        } else if (object instanceof TypeXOR) {
            xor_list.add((TypeXOR) object);
        }
    }

    private void translateObject(TypeObject obj) {
        if (obj.getDefRef() != null && def_ref.get(obj.getDefRef()) != null) {
            id_map.put(obj.getId(), def_ref.get(obj.getDefRef()));
        } else {
            ObjectType object = new ObjectType();
            id_map.put(obj.getId(), String.valueOf(ids));
            object.setId(String.valueOf(ids));
            object.setName(obj.getName());
            //object.setConfigurable(!obj.getConfigurableObject().equals(null));
            cproc.getObject().add(object);
            def_ref.put(obj.getDefRef(), String.valueOf(ids++));
        }
        obj_ref.put(obj.getId(), obj);
    }

    private void translateRole(TypeRole role) {
        if (role.getDefRef() != null && def_ref.get(role.getDefRef()) != null) {
            id_map.put(role.getId(), def_ref.get(role.getDefRef()));
        } else {
            HumanType obj = new HumanType();
            id_map.put(role.getId(), String.valueOf(ids));
            obj.setId(String.valueOf(ids));
            obj.setName(role.getName());
            cproc.getResourceType().add(obj);
            def_ref.put(role.getDefRef(), String.valueOf(ids++));
        }
        role_ref.put(role.getId(), role);
    }

    private void translateRANGE(TypeRANGE obj) {
        ObjectType object = new ObjectType();
        id_map.put(obj.getId(), String.valueOf(ids));
        object.setId(String.valueOf(ids++));
        object.setName(obj.getName());
        cproc.getObject().add(object);

        // temporary to deal with range elements problem
        TypeObject o = new TypeObject();
        o.setOptional(obj.isOptional());
        obj_ref.put(obj.getId(), o);
    }
}
