package xyz.anthofoxo.aurora.struct.sequin;

import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public class ParamPath implements ThumperStruct {
	public int paramHash;
	public int paramIdx; // typically -1

	public ParamPath() {
	}

	public ParamPath(int paramHash, int paramIdx) {
		this.paramHash = paramHash;
		this.paramIdx = paramIdx;
	}

	public ObjectNode toAur() {
		YAMLMapper mapper = new YAMLMapper();
		var node = mapper.createObjectNode();
		var revHash = Hash.hashes.get(paramHash);
		if (revHash == null) node.put("param", paramHash);
		else node.put("param", new String(revHash));
		node.put("index", paramIdx);
		return node;
	}
}