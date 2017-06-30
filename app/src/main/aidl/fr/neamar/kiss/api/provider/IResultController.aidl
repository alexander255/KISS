package fr.neamar.kiss.api.provider;

// Declare any non-default types here with import statements

interface IResultController {
	oneway void setIcon(in Bitmap icon, boolean tintIcon);
	oneway void setSubicon(in Bitmap icon, boolean tintIcon);
}
