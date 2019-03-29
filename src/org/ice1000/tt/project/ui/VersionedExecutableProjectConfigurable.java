package org.ice1000.tt.project.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@SuppressWarnings("NullableProblems")
public abstract class VersionedExecutableProjectConfigurable implements Configurable {
	protected @NotNull JPanel mainPanel;
	protected @NotNull TextFieldWithBrowseButton exePathField;
	protected @NotNull JLabel versionLabel;
	protected @NotNull JButton guessExeButton;
}