package xyz.anthofoxo.aurora;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.DSP;
import xyz.anthofoxo.aurora.struct.DSPChain;
import xyz.anthofoxo.aurora.struct.comp.dsp.DSPParamEQ;
import xyz.anthofoxo.aurora.struct.objlib.DeclarationType;
import xyz.anthofoxo.aurora.struct.objlib.ObjLib;
import xyz.anthofoxo.aurora.tml.TMLBuilder;

public class EqMod {
	private static final String PATH = "/cache/" + Integer.toHexString(Hash.fnv1a("Achannels.objlib")) + ".pc";

	public static void apply() throws IOException {
		String inPath = UserConfig.thumperPath() + PATH;

		AuroraReader in = new AuroraReader(Files.readAllBytes(Path.of(inPath)));

		ObjLib file = in.obj(ObjLib.class);

		List<String> dspRemovalCandidates = new ArrayList<>();

		for (int i = 0; i < file.objectDeclarations.size(); i++) {
			var declaration = file.objectDeclarations.get(i);

			if (declaration.type == DeclarationType.DSP) {
				var object = (DSP) file.objectDefinitions.get(file.libraryObjects.size() + i);

				for (var comp : object.comps) {
					if (comp instanceof DSPParamEQ) {
						dspRemovalCandidates.add(declaration.name);
						break;
					}
				}
			}
		}

		for (int i = 0; i < file.objectDeclarations.size(); i++) {
			var declaration = file.objectDeclarations.get(i);

			if (declaration.type == DeclarationType.DSPChain) {
				var object = (DSPChain) file.objectDefinitions.get(file.libraryObjects.size() + i);

				var iterator = object.DSPs.iterator();

				while (iterator.hasNext()) {
					var value = iterator.next();
					if (dspRemovalCandidates.contains(value)) {
						iterator.remove();
					}
				}
			}
		}

		AuroraWriter out = new AuroraWriter();
		out.obj(file);

		TMLBuilder.writefileBackedup(inPath, out.getBytes());
	}

	public static void restore() throws IOException {
		Path path = Path.of(UserConfig.thumperPath() + PATH);

		if (Files.exists(Path.of(path.toString() + ".bak"))) {
			Files.copy(Path.of(path.toString() + ".bak"), path, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
