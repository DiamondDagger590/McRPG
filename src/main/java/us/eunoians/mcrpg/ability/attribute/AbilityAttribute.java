package us.eunoians.mcrpg.ability.attribute;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This class represents some sort of value that can be tied to an ability. This class
 * should be extended for specific attributes, such as {@link AbilityTierAttribute}.
 * <p>
 * This allows for a layer of abstraction to exist for these different values and also allows
 * for future support of 3rd party plugins adding their own unique attributes to be utilized for abilities,
 * which can further modify plugin behavior.
 * <p>
 * In {@link AbilityAttributeManager}, if you get a {@link AbilityAttribute} by using {@link AbilityAttributeManager#getAttribute(NamespacedKey)} or
 * any overloaded methods, you will get an instance of this class but with only default content.
 * <p>
 * An example of this would be as follows:
 * <ul>
 * <li> I want to create a {@link AbilityTierAttribute} using only an abstracted {@link NamespacedKey}.
 * <li> To do so, I would get an {@link AbilityAttribute} from {@link AbilityAttributeManager#getAttribute(NamespacedKey)} using my provided {@link NamespacedKey}.
 * <li> This found value will be "default" and populated only with the value from {@link AbilityTierAttribute}.
 * <li> To now create a new instance with a new value, I would do {@link AbilityAttribute#create(Object)}.
 * <li> The returned {@link AbilityAttribute} will be different than the one initially obtained, as it is a newly created one and populated with the provided value
 * </ul>
 * <p>
 * The reason for this is to step away from reflection for things around the code base, while being designed to allow for scalability especially with keeping
 * 3rd party plugins in mind.
 *
 * @param <T> The type of the value that should be stored in this attribute
 */
public abstract class AbilityAttribute<T> {

    @NotNull
    private final String databaseKeyName;
    @NotNull
    private final NamespacedKey namespacedKey;
    @NotNull
    private final T content;
    @NotNull
    private final Optional<NamespacedKey> abilityType;

    protected AbilityAttribute(@NotNull String databaseKeyName, @NotNull NamespacedKey namespacedKey) {
        this.databaseKeyName = databaseKeyName;
        this.namespacedKey = namespacedKey;
        this.content = getDefaultContent();
        this.abilityType = Optional.empty();
    }

    protected AbilityAttribute(@NotNull String databaseKeyName, @NotNull NamespacedKey namespacedKey, @NotNull T content) {
        this.databaseKeyName = databaseKeyName;
        this.namespacedKey = namespacedKey;
        this.content = content;
        this.abilityType = Optional.empty();
    }

    protected AbilityAttribute(@NotNull String databaseKeyName, @NotNull NamespacedKey namespacedKey, @NotNull T content, @NotNull NamespacedKey abilityType) {
        this.databaseKeyName = databaseKeyName;
        this.namespacedKey = namespacedKey;
        this.content = content;
        this.abilityType = Optional.of(abilityType);
    }

    /**
     * Creates a new instance of this {@link AbilityAttribute} class, containing the provided {@link T} content as the value
     *
     * @param content The {@link T} content to be used as the value in the returned {@link AbilityAttribute}
     * @return A new instance of this {@link AbilityAttribute} class, containing the provided {@link T} content as the value
     */
    @NotNull
    public abstract AbilityAttribute<T> create(@NotNull T content);

    /**
     * Creates a new instance of this {@link AbilityAttribute} class. This differs from the overloaded {@link #create(Object)},
     * as this takes in a {@link String}.
     * <p>
     * This is because when working with variables using the abstract class type of {@link AbilityAttribute}, we don't always know what the intended
     * class type is when referring to {@link T}. As such, we can pass in a {@link String} to be converted into {@link T} via {@link #convertContent(String)}.
     *
     * @param rawContent The {@link String} content to be converted into {@link T} using {@link #convertContent(String)} which will be used as the value in the returned {@link AbilityAttribute}
     * @return A new instance of this {@link AbilityAttribute} class, containing the converted {@link T} content as the value
     */
    @NotNull
    public AbilityAttribute<T> create(@NotNull String rawContent) {
        return create(convertContent(rawContent));
    }

    /**
     * Gets the {@link NamespacedKey} that is associated with this attribute. This relation is mostly utilized
     * in {@link AbilityAttributeManager}.
     *
     * @return The {@link NamespacedKey} that is associated with this attribute.
     */
    @NotNull
    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    /**
     * Gets the content of type {@link T} that is stored in this attribute
     *
     * @return The content of type {@link T} that is stored in this attribute
     */
    @NotNull
    public T getContent() {
        return content;
    }

    /**
     * Converts the provided {@link String} content into content that matches the type of {@link T}.
     * <p>
     * This serves to allow abstraction to exist and all values to be stored as strings inside of {@link us.eunoians.mcrpg.database.table.SkillDAO}.
     *
     * @param stringContent The {@link String} content to be converted into type {@link T}
     * @return The {@link String} content that is now converted into {@link T} content
     */
    @NotNull
    public abstract T convertContent(@NotNull String stringContent);

    /**
     * Gets the default content value for this attribute. This should be considered the "default state" for this attribute, such
     * as a tier defaulting to 0.
     * <p>
     * The largest use case for this is populating {@link AbilityAttributeManager} with initial instances of this class, which can then
     * be built on using {@link #create(Object)}.
     *
     * @return The default {@link T} content to use.
     */
    @NotNull
    public abstract T getDefaultContent();

    /**
     * Gets the database key for this attribute to use when storing key/value pairs in the {@link us.eunoians.mcrpg.database.table.SkillDAO}.
     *
     * @return The {@link String} database key for this attribute to use when storing key/value pairs in the {@link us.eunoians.mcrpg.database.table.SkillDAO}
     */
    @NotNull
    public String getDatabaseKeyName() {
        return databaseKeyName;
    }

    /**
     * Gets the {@link NamespacedKey} that this current {@link AbilityAttribute} instance is storing data for.
     * <p>
     * In the case that this is the default instance provided by {@link AbilityAttributeManager#getAttribute(NamespacedKey)}, this will return an empty
     * {@link Optional}, as no value is being represented.
     *
     * @return An {@link Optional} containing the {@link NamespacedKey} that is having data represented by this attribute,
     * or an empty {@link Optional} if this is a default instance provided by {@link AbilityAttributeManager#getAttribute(NamespacedKey)} which represents
     * a blank attribute.
     */
    @NotNull
    public Optional<NamespacedKey> getAbilityType() {
        return abilityType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbilityAttribute<?> abilityAttribute) {
            return abilityAttribute.getNamespacedKey().equals(this.getNamespacedKey()) && abilityAttribute.getDatabaseKeyName().equals(this.getDatabaseKeyName()) && abilityAttribute.getContent().equals(this.getContent()) && (abilityAttribute.getAbilityType().isPresent() == this.getAbilityType().isPresent());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getNamespacedKey().hashCode();
        hash = 31 * hash + getDatabaseKeyName().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "Ability Type: " + (abilityType.isPresent() ? abilityType.get() : "null")
                + " Namespaced Key: " + getNamespacedKey() + " Content: " + getContent()
                + " Database Key: " + getDatabaseKeyName();
    }
}
