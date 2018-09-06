package us.eunoians.mcmmox.Abilities;

import lombok.Getter;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.types.DefaultAbilities;
import us.eunoians.mcmmox.util.Parser;

public class Bleed extends BaseAbility {

  @Getter
  private static Parser bleedChanceEquation = new Parser(Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getString("BleedConfig.BleedChanceEquation"));

  public Bleed() {
    super(DefaultAbilities.BLEED, true);
  }
}