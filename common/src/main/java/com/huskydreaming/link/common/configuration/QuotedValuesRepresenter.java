package com.huskydreaming.link.common.configuration;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Map;

/**
 * A SnakeYAML {@link Representer} that produces clean, human-readable YAML output:
 * <ul>
 *   <li>String values are double-quoted for readability and YAML safety.</li>
 *   <li>Numeric, boolean, and null scalars are left as plain scalars — no {@code !!int} or {@code !!bool} type tags.</li>
 *   <li>Mapping keys are always plain scalars.</li>
 * </ul>
 */
class QuotedValuesRepresenter extends Representer {

    QuotedValuesRepresenter(DumperOptions options) {
        super(options);
    }

    @Override
    protected Node representScalar(Tag tag, String value, DumperOptions.ScalarStyle style) {
        // Double-quote genuine string values; leave numbers, booleans and nulls as plain.
        if (Tag.STR.equals(tag)) {
            return super.representScalar(tag, value, DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        }
        return super.representScalar(tag, value, DumperOptions.ScalarStyle.PLAIN);
    }

    @Override
    protected Node representMapping(Tag tag, Map<?, ?> mapping, DumperOptions.FlowStyle flowStyle) {
        var node = super.representMapping(tag, mapping, flowStyle);
        if (!(node instanceof MappingNode mappingNode)) {
            return node;
        }

        var tuples = mappingNode.getValue();
        for (var i = 0; i < tuples.size(); i++) {
            if (tuples.get(i).getKeyNode() instanceof ScalarNode keyScalar) {
                var plainKey = new ScalarNode(
                        keyScalar.getTag(),
                        keyScalar.getValue(),
                        null,
                        null,
                        DumperOptions.ScalarStyle.PLAIN
                );
                tuples.set(i, new NodeTuple(plainKey, tuples.get(i).getValueNode()));
            }
        }
        return node;
    }
}