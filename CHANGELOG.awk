BEGIN {
	printf("<?xml version='1.0' encoding='utf-8'?>\n");
	printf("<resources>\n");
	printf("<string name='whats_new_dialog_text' translatable='false'>\n");
}
{
	printf("%s\\n\n", $0);
}
END {
	printf("</string>\n");
	printf("</resources>\n");
}
